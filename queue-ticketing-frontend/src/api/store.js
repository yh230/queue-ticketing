import request from './request';

export const storeApi = {
  listByMerchant(merchantId) {
    return request.get(`/stores/merchant/${merchantId}`);
  }
};

export const counterApi = {
  listByStore(storeId) {
    return request.get(`/counters/store/${storeId}`);
  }
};
