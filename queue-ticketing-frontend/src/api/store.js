import request from './request';

export const storeApi = {
  get(storeId) {
    return request.get(`/stores/${storeId}`);
  },
  listByMerchant(merchantId) {
    return request.get(`/stores/merchant/${merchantId}`);
  }
};

export const counterApi = {
  listByStore(storeId) {
    return request.get(`/counters/store/${storeId}`);
  }
};
