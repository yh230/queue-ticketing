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
            <span>取号结果</span>
          </template>
          <el-result icon="success" :title="`您的号码：${ticket.ticketNo}`">
            <template #sub-title>
              <p>前方还有 {{ ticket.aheadCount ?? 0 }} 人</p>
              <p>预计等待 {{ ticket.estimatedWaitMinutes ?? 0 }} 分钟</p>
              <p>状态：{{ ticket.statusDesc }}</p>
            </template>
            <template #extra>
              <el-button @click="refresh">刷新进度</el-button>
              <el-button type="danger" plain @click="cancel">取消取号</el-button>
            </template>
          </el-result>
        </el-card>
        <el-empty v-else description="取号后在这里查看结果" />
      </el-col>
    </el-row>
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
  ElMessage.warning('已取消取号');
  ticket.value = null;
};
</script>
