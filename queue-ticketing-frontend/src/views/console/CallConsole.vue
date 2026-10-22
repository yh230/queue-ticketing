<template>
  <div class="page">
    <el-card class="store-card">
      <div class="store-info">
        <span class="store-label">门店</span>
        <span class="store-name">{{ storeName || '加载中...' }}</span>
        <el-tag type="info" size="small">ID: {{ storeId }}</el-tag>
      </div>
    </el-card>

    <el-card class="overview-card">
      <template #header>
        <div class="header">
          <span>队列总览</span>
          <el-button size="small" :loading="loading" @click="refresh">刷新</el-button>
        </div>
      </template>
      <el-table v-loading="loading" :data="overview" border>
        <el-table-column prop="queueInstanceId" label="实例 ID" width="100" />
        <el-table-column prop="queueName" label="队列名称" min-width="150" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 'OPEN' ? 'success' : 'info'" size="small">
              {{ statusMap[row.status] || row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="waitCount" label="等待人数" width="100" />
        <el-table-column label="当前叫号" width="240">
          <template #default="{ row }">
            <template v-if="row.currentTicketNo">
              <el-tag type="warning" class="called-no">{{ row.currentTicketNo }}</el-tag>
              <el-button size="small" type="success" @click="serve(row)">完成</el-button>
              <el-button size="small" type="warning" @click="miss(row)">过号</el-button>
            </template>
            <span v-else class="empty-text">-</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120">
          <template #default="{ row }">
            <el-button
              size="small"
              type="primary"
              :loading="callingQueueId === Number(row.queueInstanceId)"
              @click="callQueue(row)"
            >
              下一位
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-row :gutter="20">
      <el-col v-for="queue in overview" :key="queue.queueInstanceId" :span="12">
        <el-card class="waiting-card">
          <template #header>
            <div class="header">
              <span>{{ queue.queueName }} · 等待队列前 10 位</span>
              <el-tag type="info" size="small">{{ queue.waitCount ?? 0 }} 人</el-tag>
            </div>
          </template>
          <el-table :data="(queue.waitingTickets || []).slice(0, 10)" border max-height="360">
            <el-table-column type="index" label="#" width="60" />
            <el-table-column prop="ticketNo" label="号码" width="100" />
            <el-table-column prop="sourceDesc" label="来源" width="100" />
            <el-table-column prop="userPhone" label="手机号" width="130">
              <template #default="{ row }">{{ row.userPhone || '-' }}</template>
            </el-table-column>
            <el-table-column prop="createdAt" label="取号时间" />
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { onMounted, onUnmounted, ref } from 'vue';
import { useRoute } from 'vue-router';
import { ElMessage } from 'element-plus';
import { storeApi, ticketApi } from '../../api';

const route = useRoute();
const storeId = Number(route.query.storeId) || 1;
const counterId = Number(route.query.counterId) || 1;

const storeName = ref('');
const overview = ref([]);
const loading = ref(false);
const callingQueueId = ref(null);
let timer = null;

const statusMap = {
  OPEN: '开放中',
  PAUSED: '已暂停',
  CLOSED: '已关闭'
};

const loadStore = async () => {
  if (storeName.value) {
    return;
  }
  try {
    const store = await storeApi.get(storeId);
    storeName.value = store?.name || '';
  } catch (e) {
    storeName.value = '';
  }
};

const refresh = async () => {
  loading.value = true;
  try {
    overview.value = await ticketApi.getQueueOverview(storeId);
  } catch (e) {
    // 轮询失败不打断页面
  } finally {
    loading.value = false;
  }
};

const callQueue = async (row) => {
  if (row.currentTicketNo) {
    ElMessage.warning(`请先完成或过号「${row.queueName}」的 ${row.currentTicketNo}`);
    return;
  }
  callingQueueId.value = Number(row.queueInstanceId);
  try {
    const ticket = await ticketApi.call({
      queueInstanceId: Number(row.queueInstanceId),
      counterId
    });
    ElMessage.success(`「${row.queueName}」叫号：${ticket.ticketNo}`);
    await refresh();
  } finally {
    callingQueueId.value = null;
  }
};

const serve = async (row) => {
  if (!row.currentTicketId) return;
  try {
    await ticketApi.serve(row.currentTicketId);
    ElMessage.success(`「${row.queueName}」${row.currentTicketNo} 已完成`);
  } finally {
    await refresh();
  }
};

const miss = async (row) => {
  if (!row.currentTicketId) return;
  try {
    await ticketApi.miss({ ticketId: row.currentTicketId });
    ElMessage.warning(`「${row.queueName}」${row.currentTicketNo} 已过号`);
  } finally {
    await refresh();
  }
};

onMounted(() => {
  loadStore();
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
.store-card {
  margin-bottom: 20px;
}

.store-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.store-label {
  color: #909399;
}

.store-name {
  font-size: 22px;
  font-weight: 600;
  color: #0d9488;
}

.overview-card {
  margin-bottom: 20px;
}

.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.called-no {
  margin-right: 8px;
}

.empty-text {
  color: #c0c4cc;
}

.waiting-card {
  margin-bottom: 20px;
}
</style>
