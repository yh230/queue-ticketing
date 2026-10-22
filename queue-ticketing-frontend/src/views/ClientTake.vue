<template>
  <div class="h5">
    <div class="h5-card">
      <h2>线上取号</h2>

      <template v-if="!ticket">
        <el-form :model="form" label-position="top">
          <el-form-item label="手机号">
            <el-input v-model="form.phone" placeholder="请输入手机号" maxlength="11" />
          </el-form-item>
          <el-form-item label="门店">
            <el-select v-model="form.storeId" style="width: 100%">
              <el-option label="示例餐厅·人民广场店" :value="1" />
            </el-select>
          </el-form-item>
          <el-form-item label="桌型">
            <el-select v-model="form.templateId" style="width: 100%">
              <el-option label="小桌（1-2人）" :value="1" />
              <el-option label="大桌（3-6人）" :value="2" />
            </el-select>
          </el-form-item>
          <el-button type="primary" style="width: 100%" :loading="loading" @click="takeTicket">
            立即取号
          </el-button>
        </el-form>
      </template>

      <template v-else>
        <div class="result">
          <p class="label">您的排队号码</p>
          <p class="number">{{ ticket.ticketNo }}</p>
          <p>前方还有 <strong>{{ ticket.aheadCount ?? 0 }}</strong> 人</p>
          <p>预计等待 <strong>{{ ticket.estimatedWaitMinutes ?? 0 }}</strong> 分钟</p>
          <p class="status">{{ ticket.statusDesc }}</p>
        </div>

        <div class="actions">
          <el-button @click="refresh">刷新进度</el-button>
          <el-button type="danger" plain @click="cancel">取消取号</el-button>
        </div>
      </template>

      <p class="tip">演示环境，取号与通知均为模拟。</p>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue';
import { ElMessage } from 'element-plus';
import { clientApi, ticketApi } from '../api';

const loading = ref(false);
const ticket = ref(null);
const clientToken = ref('');

const form = reactive({
  phone: '13800000000',
  storeId: 1,
  templateId: 1
});

const takeTicket = async () => {
  if (!/^1\d{10}$/.test(form.phone)) {
    ElMessage.warning('请输入正确的手机号');
    return;
  }
  loading.value = true;
  try {
    clientToken.value = await clientApi.login({ phone: form.phone });
    ticket.value = await ticketApi.take({
      storeId: form.storeId,
      templateId: form.templateId,
      source: 'MINI_PROGRAM',
      userPhone: form.phone,
      idempotencyKey: `${form.phone}-${Date.now()}`
    }, clientToken.value);
    ElMessage.success('取号成功');
  } finally {
    loading.value = false;
  }
};

const refresh = async () => {
  if (!ticket.value) return;
  ticket.value = await ticketApi.get(ticket.value.id);
};

const cancel = async () => {
  if (!ticket.value) return;
  await ticketApi.cancel({ ticketId: ticket.value.id });
  ticket.value = null;
  ElMessage.warning('已取消取号');
};
</script>

<style scoped>
.h5 {
  min-height: 100vh;
  padding: 20px;
  background: linear-gradient(160deg, #e6f7f5, #f5f7fa);
}

.h5-card {
  max-width: 420px;
  margin: 40px auto;
  padding: 24px;
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 12px 30px rgba(13, 148, 136, 0.12);
}

.h5-card h2 {
  margin-top: 0;
  color: #0d9488;
  text-align: center;
}

.result {
  text-align: center;
}

.result .label {
  color: #666;
}

.result .number {
  font-size: 56px;
  font-weight: 700;
  color: #0d9488;
  margin: 8px 0 16px;
}

.result .status {
  color: #999;
}

.actions {
  display: flex;
  gap: 12px;
  margin-top: 20px;
}

.actions .el-button {
  flex: 1;
}

.tip {
  margin-top: 20px;
  color: #aaa;
  font-size: 12px;
  text-align: center;
}
</style>
