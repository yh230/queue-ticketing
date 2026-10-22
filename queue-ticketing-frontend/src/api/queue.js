import request from './request';

export const queueApi = {
  listTemplates(merchantId) {
    return request.get(`/queue-templates/merchant/${merchantId}`);
  },
  getOrCreateInstance(storeId, templateId) {
    return request.get('/queue-instances', { params: { storeId, templateId } });
  }
};
