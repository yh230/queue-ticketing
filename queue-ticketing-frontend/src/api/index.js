import request from './request';

export const staffApi = {
  login(data) {
    return request.post('/staff/login', data);
  }
};

export const clientApi = {
  login(data) {
    return request.post('/client/login', data);
  }
};

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

export const queueApi = {
  listTemplates(merchantId) {
    return request.get(`/queue-templates/merchant/${merchantId}`);
  },
  getOrCreateInstance(storeId, templateId) {
    return request.get('/queue-instances', { params: { storeId, templateId } });
  }
};

export const ticketApi = {
  take(data, token) {
    const headers = { 'Idempotency-Key': data.idempotencyKey };
    if (token) {
      headers.Authorization = `Bearer ${token}`;
    }
    return request.post('/tickets/take', data, { headers });
  },
  get(ticketId) {
    return request.get(`/tickets/${ticketId}`);
  },
  cancel(data) {
    return request.post('/tickets/cancel', data);
  },
  call(data) {
    return request.post('/tickets/call', data);
  },
  miss(data) {
    return request.post('/tickets/miss', data);
  },
  serve(ticketId) {
    return request.post(`/tickets/${ticketId}/serve`);
  }
};
