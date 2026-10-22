<template>
  <div class="page">
    <el-card>
      <template #header>
        <span>叫号控制台</span>
      </template>

      <el-form :model="form" label-width="120px" class="toolbar">
        <el-form-item label="队列实例 ID">
          <el-input v-model.number="form.queueInstanceId" style="width: 180px" />
        </el-form-item>
        <el-form-item label="柜台 ID">
          <el-input v-model.number="form.counterId" style="width: 180px" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="calling" @click="callNext">下一位</el-button>
        </el-form-item>
      </el-form>

      <el-empty v-if="!currentTicket" description="点击「下一位」开始叫号" />

      <el-card v-else shadow="never">
        <el-result icon="info" :title="`当前叫号：${currentTicket.ticketNo}`">
          <template #sub-title>
            <p>状态：{{ currentTicket.statusDesc }}</p>
            <p>用户：{{ currentTicket.userId }}</p>
          </template>
          <template #extra>
            <el-button type="success" @click="serve">标记完成</el-button>
            <el-button type="warning" @click="miss">过号</el-button>
          </template>
        </el-result>
      </el-card>
    </el-card>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue';
import { useRoute } from 'vue-router';
import { ElMessage } from 'element-plus';
import { ticketApi } from '../api';

const route = useRoute();
const calling = ref(false);
const currentTicket = ref(null);

const form = reactive({
  queueInstanceId: Number(route.query.instanceId) || 1,
  counterId: Number(route.query.counterId) || 1
});

const callNext = async () => {
  calling.value = true;
  try {
    currentTicket.value = await ticketApi.call({
      queueInstanceId: form.queueInstanceId,
      counterId: form.counterId
    });
    ElMessage.success('叫号成功');
  } finally {
    calling.value = false;
  }
};

const serve = async () => {
  if (!currentTicket.value) return;
  currentTicket.value = await ticketApi.serve(currentTicket.value.id);
  ElMessage.success('已标记完成');
};

const miss = async () => {
  if (!currentTicket.value) return;
  currentTicket.value = await ticketApi.miss({ ticketId: currentTicket.value.id });
  ElMessage.warning('已标记过号');
};
</script>

<style scoped>
.toolbar {
  margin-bottom: 20px;
}
</style>
