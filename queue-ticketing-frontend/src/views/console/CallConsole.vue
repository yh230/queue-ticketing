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
        <el-form-item label="队列实例 ID">
          <el-input v-model.number="form.queueInstanceId" style="width: 180px" @change="refresh" />
        </el-form-item>
        <el-form-item label="柜台 ID">
          <el-input v-model.number="form.counterId" style="width: 180px" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="calling" @click="callNext">下一位</el-button>
          <el-button @click="refresh">刷新</el-button>
        </el-form-item>
      </el-form>

      <el-row :gutter="20">
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
            <el-table :data="queueStatus?.waitingTickets ?? []" border max-height="420">
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
const queueStatus = ref(null);
const currentTicket = ref(null);
let timer = null;

const form = reactive({
  queueInstanceId: Number(route.query.instanceId) || 1,
  counterId: Number(route.query.counterId) || 1
});

const statusText = computed(() => {
  const status = queueStatus.value?.status;
  return { OPEN: '开放中', PAUSED: '已暂停', CLOSED: '已关闭' }[status] || '未知';
});

const statusTagType = computed(() => {
  const status = queueStatus.value?.status;
  return { OPEN: 'success', PAUSED: 'warning', CLOSED: 'info' }[status] || 'info';
});

const refresh = async () => {
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
    // 轮询失败不打断操作
  }
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
  margin-bottom: 20px;
}
</style>
