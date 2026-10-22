<template>
  <div class="page">
    <el-row :gutter="20">
      <el-col :span="12">
        <el-card>
          <template #header>
            <span>取号模拟（模拟 C 端用户）</span>
          </template>
          <el-form :model="form" label-width="120px">
            <el-form-item label="手机号">
              <el-input v-model="form.phone" placeholder="用于生成 C 端身份" />
            </el-form-item>
            <el-form-item label="门店 ID">
              <el-input v-model.number="form.storeId" />
            </el-form-item>
            <el-form-item label="队列模板 ID">
              <el-input v-model.number="form.templateId" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="loading" @click="takeTicket">取号</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>

      <el-col :span="12">
        <el-card v-if="ticket">
          <template #header>
            <div class="header">
              <span>取号结果</span>
              <el-tag v-if="isActive" type="success" size="small">自动刷新中</el-tag>
            </div>
          </template>
          <el-result :icon="resultIcon" :title="`您的号码：${ticket.ticketNo}`">
            <template #sub-title>
              <p>前方还有 {{ ticket.aheadCount ?? 0 }} 人</p>
              <p>预计等待 {{ ticket.estimatedWaitMinutes ?? 0 }} 分钟</p>
              <p>状态：{{ ticket.statusDesc }}</p>
            </template>
            <template #extra>
              <el-button @click="refresh">刷新进度</el-button>
              <el-button v-if="isActive" type="danger" plain @click="cancel">取消取号</el-button>
            </template>
          </el-result>
        </el-card>
        <el-empty v-else description="取号后在这里查看结果" />
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { computed, onUnmounted, reactive, ref } from 'vue';
import { ElMessage } from 'element-plus';
import { clientApi, ticketApi } from '../../api';

const loading = ref(false);
const ticket = ref(null);
const clientToken = ref('');
let timer = null;

const form = reactive({
  phone: '13800000000',
  storeId: 1,
  templateId: 1
});

const isActive = computed(() => ticket.value
  && ['WAITING', 'CALLED'].includes(ticket.value.status));

const resultIcon = computed(() => {
  if (!ticket.value) return 'info';
  return ticket.value.status === 'CALLED' ? 'warning' : 'success';
});

const stopPolling = () => {
  if (timer) {
    clearInterval(timer);
    timer = null;
  }
};

const startPolling = () => {
  stopPolling();
  timer = setInterval(refresh, 5000);
};

const takeTicket = async () => {
  if (!form.phone) {
    ElMessage.warning('请输入手机号');
    return;
  }
  loading.value = true;
  try {
    clientToken.value = await clientApi.login({ phone: form.phone });
    const idempotencyKey = `${form.phone}-${Date.now()}`;
    ticket.value = await ticketApi.take({
      storeId: form.storeId,
      templateId: form.templateId,
      source: 'MINI_PROGRAM',
      userPhone: form.phone,
      idempotencyKey
    }, clientToken.value);
    ElMessage.success('取号成功');
    startPolling();
  } finally {
    loading.value = false;
  }
};

const refresh = async () => {
  if (!ticket.value) return;
  try {
    const latest = await ticketApi.get(ticket.value.id);
    if (latest.status === 'CALLED' && ticket.value.status !== 'CALLED') {
      ElMessage.warning('已叫号，请尽快到店');
    }
    if (latest.status === 'MISSED') {
      ElMessage.error('已过号');
    }
    ticket.value = latest;
    if (!['WAITING', 'CALLED'].includes(latest.status)) {
      stopPolling();
    }
  } catch (e) {
    // 轮询失败不打断页面
  }
};

const cancel = async () => {
  if (!ticket.value) return;
  await ticketApi.cancel({ ticketId: ticket.value.id });
  ElMessage.warning('已取消取号');
  ticket.value = null;
  stopPolling();
};

onUnmounted(stopPolling);
</script>

<style scoped>
.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
</style>
