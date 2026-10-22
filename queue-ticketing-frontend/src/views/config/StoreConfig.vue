<template>
  <div class="page">
    <el-card>
      <template #header>
        <span>门店与队列配置</span>
      </template>

      <el-form label-width="120px" class="toolbar">
        <el-form-item label="商户 ID">
          <el-input v-model.number="merchantId" style="width: 160px" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData">加载</el-button>
        </el-form-item>
      </el-form>

      <el-row :gutter="20">
        <el-col :span="12">
          <el-card shadow="never">
            <template #header>门店列表</template>
            <el-table :data="stores" border>
              <el-table-column prop="id" label="ID" width="80" />
              <el-table-column prop="name" label="门店名称" />
              <el-table-column prop="businessHours" label="营业时间" />
            </el-table>
          </el-card>
        </el-col>

        <el-col :span="12">
          <el-card shadow="never">
            <template #header>队列模板</template>
            <el-table :data="templates" border>
              <el-table-column prop="id" label="ID" width="80" />
              <el-table-column prop="name" label="队列名称" />
              <el-table-column prop="prefix" label="前缀" width="80" />
              <el-table-column prop="capacity" label="容量" width="80" />
            </el-table>
          </el-card>
        </el-col>
      </el-row>

      <el-card shadow="never" class="instances">
        <template #header>今日队列实例</template>
        <el-table :data="instances" border>
          <el-table-column prop="id" label="实例 ID" width="100" />
          <el-table-column prop="storeId" label="门店 ID" width="100" />
          <el-table-column prop="templateId" label="模板 ID" width="100" />
          <el-table-column prop="queueDate" label="日期" width="140" />
          <el-table-column prop="status" label="状态" width="120" />
          <el-table-column prop="waitCount" label="等待人数" width="100" />
          <el-table-column label="操作">
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
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { storeApi, queueApi } from '../../api';

const router = useRouter();
const merchantId = ref(1);
const stores = ref([]);
const templates = ref([]);
const instances = ref([]);

const loadData = async () => {
  stores.value = await storeApi.listByMerchant(merchantId.value);
  templates.value = await queueApi.listTemplates(merchantId.value);

  instances.value = [];
  for (const store of stores.value) {
    for (const template of templates.value) {
      try {
        const instance = await queueApi.getOrCreateInstance(store.id, template.id);
        instances.value.push(instance);
      } catch (e) {
        // 单个队列加载失败不影响其他队列
      }
    }
  }
  ElMessage.success('加载完成');
};

const goConsole = (row) => {
  router.push({ path: '/console', query: { instanceId: row.id, counterId: 1 } });
};

loadData();
</script>

<style scoped>
.toolbar {
  margin-bottom: 10px;
}

.instances {
  margin-top: 20px;
}
</style>
