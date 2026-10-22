import request from './request';

const authHeaders = (token) => (token ? { Authorization: `Bearer ${token}` } : {});

export const queueCenterApi = {
  take(data, token) {
    const requestId = data.requestId
      || `${data.storeId}-${data.templateId}-${Date.now()}-${Math.random().toString(36).slice(2)}`;
    return request.post('/queue/take', data, {
      headers: { 'Idempotency-Key': requestId, ...authHeaders(token) }
    });
  },

  progress(storeId, templateId, ticketNo, token) {
    return request.get('/queue/progress', {
      params: { storeId, templateId, ticketNo },
      headers: authHeaders(token)
    });
  },

  my(storeId, token) {
    return request.get('/queue/my', {
      params: { storeId },
      headers: authHeaders(token)
    });
  },

  cancel(data, token) {
    return request.post('/queue/cancel', data, { headers: authHeaders(token) });
  },

  call(data) {
    return request.post('/queue/call', data);
  },

  finish(data) {
    return request.post('/queue/finish', data);
  },

  miss(data) {
    return request.post('/queue/miss', data);
  },

  overview(storeId) {
    return request.get('/queue/overview', { params: { storeId } });
  }
};
