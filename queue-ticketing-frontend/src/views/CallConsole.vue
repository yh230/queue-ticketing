<template>
  <div class="call-console">
    <el-card>
      <template #header>
        <span>叫号控制台</span>
      </template>
      <el-form :model="form" label-width="120px">
        <el-form-item label="队列实例ID">
          <el-input v-model="form.queueInstanceId" placeholder="请输入队列实例ID" />
        </el-form-item>
        <el-form-item label="柜台ID">
          <el-input v-model="form.counterId" placeholder="请输入柜台ID" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="callNext">下一位</el-button>
          <el-button @click="resetForm">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card v-if="currentTicket" style="margin-top: 20px;">
      <template #header>
        <span>当前叫号</span>
      </template>
      <p>号码：{{ currentTicket.ticketNo }}</p>
      <p>状态：{{ currentTicket.statusDesc }}</p>
      <p>用户：{{ currentTicket.userId }}</p>
      <el-button type="success" @click="serveTicket">标记完成</el-button>
      <el-button type="warning" @click="missTicket">过号</el-button>
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import axios from 'axios';
import { ElMessage } from 'element-plus';

const form = ref({
  queueInstanceId: '',
  counterId: ''
});

const currentTicket = ref(null);

const callNext = async () => {
  try {
    const res = await axios.post('/api/v1/tickets/call', {
      queueInstanceId: Number(form.value.queueInstanceId),
      counterId: Number(form.value.counterId)
    });
    if (res.data.code === 200) {
      currentTicket.value = res.data.data;
      ElMessage.success('叫号成功');
    } else {
      ElMessage.error(res.data.message);
    }
  } catch (e) {
    ElMessage.error('叫号失败');
  }
};

const serveTicket = async () => {
  if (!currentTicket.value) return;
  try {
    const res = await axios.post(`/api/v1/tickets/${currentTicket.value.id}/serve`);
    if (res.data.code === 200) {
      currentTicket.value = res.data.data;
      ElMessage.success('标记完成');
    } else {
      ElMessage.error(res.data.message);
    }
  } catch (e) {
    ElMessage.error('操作失败');
  }
};

const missTicket = async () => {
  if (!currentTicket.value) return;
  try {
    const res = await axios.post('/api/v1/tickets/miss', {
      ticketId: currentTicket.value.id
    });
    if (res.data.code === 200) {
      currentTicket.value = res.data.data;
      ElMessage.warning('已标记过号');
    } else {
      ElMessage.error(res.data.message);
    }
  } catch (e) {
    ElMessage.error('操作失败');
  }
};

const resetForm = () => {
  form.value = { queueInstanceId: '', counterId: '' };
  currentTicket.value = null;
};
</script>
