<template>
  <div class="page">
    <el-card>
      <template #header>
        <div class="header">
          <span>门店与队列配置</span>
          <el-button type="primary" :loading="loading" @click="loadData">刷新</el-button>
        </div>
      </template>

      <el-form label-width="120px" class="toolbar">
        <el-form-item label="商户 ID">
          <el-input v-model.number="merchantId" style="width: 160px" />
        </el-form-item>
      </el-form>

      <el-row :gutter="20">
        <el-col :span="12">
          <el-card shadow="never">
            <template #header>门店列表</template>
            <el-table v-loading="loading" :data="stores" border>
              <el-table-column prop="id" label="ID" width="80" />
              <el-table-column prop="name" label="门店名称" />
              <el-table-column prop="businessHours" label="营业时间" />
            </el-table>
          </el-card>
        </el-col>

        <el-col :span="12">
          <el-card shadow="never">
            <template #header>队列模板</template>
            <el-table v-loading="loading" :data="templates" border>
              <el-table-column prop="id" label="ID" width="80" />
              <el-table-column prop="name" label="队列名称" />
              <el-table-column prop="prefix" label="前缀" width="80" />
              <el-table-column prop="capacity" label="容量" width="80" />
            </el-table>
          </el-card>
        </el-col>
      </el-row>

      <el-card shadow="never" class="instances">
        <template #header>实时队列状态（Redis）</template>
        <el-table v-loading="loading" :data="queues" border>
          <el-table-column prop="storeId" label="门店 ID" width="100" />
          <el-table-column prop="templateId" label="模板 ID" width="100" />
          <el-table-column prop="queueName" label="队列名称" min-width="150" />
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
          <el-table-column label="操作" width="120">
            <template #default="{ row }">
              <el-button size="small" @click="goConsole(row)">去叫号</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-card>
    </el-card>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { storeApi, queueApi, queueCenterApi } from '../../api';

const router = useRouter();
const merchantId = ref(1);
const loading = ref(false);
const stores = ref([]);
const templates = ref([]);
const queues = ref([]);

const statusMap = {
  OPEN: '开放中',
  PAUSED: '已暂停',
  CLOSED: '已关闭'
};

const loadData = async () => {
  loading.value = true;
  try {
    stores.value = await storeApi.listByMerchant(merchantId.value);
    templates.value = await queueApi.listTemplates(merchantId.value);

    const results = [];
    for (const store of stores.value) {
      const list = await queueCenterApi.overview(store.id);
      results.push(...list);
    }
    queues.value = results;
  } catch (e) {
    ElMessage.error('加载失败，请检查后端服务');
  } finally {
    loading.value = false;
  }
};

const goConsole = (row) => {
  router.push({
    path: '/console',
    query: { storeId: row.storeId, templateId: row.templateId, counterId: 1 }
  });
};

onMounted(loadData);
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

.instances {
  margin-top: 20px;
}
</style>
