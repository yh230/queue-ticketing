import request from './request';

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
  },
  getQueueStatus(queueInstanceId) {
    return request.get(`/tickets/queue/${queueInstanceId}/status`);
  },
  getActive(storeId, templateId, token) {
    const headers = token ? { Authorization: `Bearer ${token}` } : {};
    return request.get('/tickets/active', {
      params: { storeId, templateId },
      headers
    });
  }
};
