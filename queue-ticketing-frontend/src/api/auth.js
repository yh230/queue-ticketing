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
