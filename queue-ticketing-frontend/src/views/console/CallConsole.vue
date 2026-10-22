<template>
  <div class="page">
    <el-card>
      <template #header>
        <div class="header">
          <span>叫号控制台</span>
          <el-tag :type="statusTagType">{{ statusText }}</el-tag>
        </div>
      </template>

      <el-form :model="form" label-width="120px" class="toolbar">
        <el-form-item label="门店 ID">
          <el-input v-model.number="form.storeId" style="width: 160px" @change="refresh" />
        </el-form-item>
        <el-form-item label="当前队列">
          <el-input v-model.number="form.queueInstanceId" style="width: 160px" @change="refresh" />
        </el-form-item>
        <el-form-item label="柜台 ID">
          <el-input v-model.number="form.counterId" style="width: 160px" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="calling" @click="callNext">下一位</el-button>
          <el-button @click="refresh">刷新</el-button>
        </el-form-item>
      </el-form>

      <el-card shadow="never" class="overview">
        <template #header>队列总览</template>
        <el-table
          v-loading="loadingOverview"
          :data="overview"
          border
          highlight-current-row
          @row-click="selectQueue"
        >
          <el-table-column prop="queueInstanceId" label="实例 ID" width="100" />
          <el-table-column prop="queueName" label="队列名称" min-width="140" />
          <el-table-column prop="status" label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="row.status === 'OPEN' ? 'success' : 'info'" size="small">
                {{ statusMap[row.status] || row.status }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="waitCount" label="等待人数" width="100" />
          <el-table-column prop="currentTicketNo" label="当前叫号" width="120">
            <template #default="{ row }">{{ row.currentTicketNo || '-' }}</template>
          </el-table-column>
          <el-table-column label="操作" width="100">
            <template #default="{ row }">
              <el-button size="small" @click.stop="selectQueue(row)">选择</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-card>

      <el-row :gutter="20" class="detail">
        <el-col :span="10">
          <el-card shadow="never">
            <template #header>当前叫号</template>
            <el-result v-if="currentTicket" icon="info" :title="currentTicket.ticketNo">
              <template #sub-title>
                <p>{{ currentTicket.statusDesc || '已叫号' }}</p>
              </template>
              <template #extra>
                <el-button type="success" @click="serve">标记完成</el-button>
                <el-button type="warning" @click="miss">过号</el-button>
              </template>
            </el-result>
            <el-empty v-else description="暂无叫号" />
          </el-card>
        </el-col>

        <el-col :span="14">
          <el-card shadow="never">
            <template #header>等待队列（{{ queueStatus?.waitCount ?? 0 }} 人）</template>
            <el-table :data="queueStatus?.waitingTickets ?? []" border max-height="360">
              <el-table-column type="index" label="#" width="60" />
              <el-table-column prop="ticketNo" label="号码" width="120" />
              <el-table-column prop="createdAt" label="取号时间" />
            </el-table>
          </el-card>
        </el-col>
      </el-row>
    </el-card>
  </div>
</template>

<script setup>
import { computed, onMounted, onUnmounted, reactive, ref } from 'vue';
import { useRoute } from 'vue-router';
import { ElMessage } from 'element-plus';
import { ticketApi } from '../../api';

const route = useRoute();
const calling = ref(false);
const loadingOverview = ref(false);
const overview = ref([]);
const queueStatus = ref(null);
const currentTicket = ref(null);
let timer = null;

const form = reactive({
  storeId: Number(route.query.storeId) || 1,
  queueInstanceId: Number(route.query.instanceId) || 1,
  counterId: Number(route.query.counterId) || 1
});

const statusMap = {
  OPEN: '开放中',
  PAUSED: '已暂停',
  CLOSED: '已关闭'
};

const statusText = computed(() => statusMap[queueStatus.value?.status] || '未知');

const statusTagType = computed(() => {
  const status = queueStatus.value?.status;
  return { OPEN: 'success', PAUSED: 'warning', CLOSED: 'info' }[status] || 'info';
});

const refresh = async () => {
  loadingOverview.value = true;
  try {
    overview.value = await ticketApi.getQueueOverview(form.storeId);
  } catch (e) {
    // 总览失败不打断叫号
  } finally {
    loadingOverview.value = false;
  }

  try {
    queueStatus.value = await ticketApi.getQueueStatus(form.queueInstanceId);
    if (queueStatus.value.currentTicketId) {
      currentTicket.value = {
        id: queueStatus.value.currentTicketId,
        ticketNo: queueStatus.value.currentTicketNo,
        statusDesc: '已叫号'
      };
    } else {
      currentTicket.value = null;
    }
  } catch (e) {
    queueStatus.value = null;
    currentTicket.value = null;
  }
};

const selectQueue = (row) => {
  form.queueInstanceId = Number(row.queueInstanceId);
  refresh();
};

const callNext = async () => {
  if (currentTicket.value) {
    ElMessage.warning('请先完成或过号当前号码');
    return;
  }
  calling.value = true;
  try {
    const ticket = await ticketApi.call({
      queueInstanceId: form.queueInstanceId,
      counterId: form.counterId
    });
    currentTicket.value = ticket;
    ElMessage.success('叫号成功');
    await refresh();
  } finally {
    calling.value = false;
  }
};

const serve = async () => {
  if (!currentTicket.value) return;
  await ticketApi.serve(currentTicket.value.id);
  ElMessage.success('已标记完成');
  currentTicket.value = null;
  await refresh();
};

const miss = async () => {
  if (!currentTicket.value) return;
  await ticketApi.miss({ ticketId: currentTicket.value.id });
  ElMessage.warning('已标记过号');
  currentTicket.value = null;
  await refresh();
};

onMounted(() => {
  refresh();
  timer = setInterval(refresh, 3000);
});

onUnmounted(() => {
  if (timer) {
    clearInterval(timer);
  }
});
</script>

<style scoped>
.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.toolbar {
  margin-bottom: 10px;
}

.overview {
  margin-bottom: 20px;
}

.detail {
  margin-top: 4px;
}
</style>
