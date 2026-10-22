import axios from 'axios';
import { ElMessage } from 'element-plus';
import { getToken, removeToken } from '../utils/auth';

const request = axios.create({
  baseURL: '/api/v1',
  timeout: 10000
});

request.interceptors.request.use((config) => {
  if (!config.headers.Authorization) {
    const token = getToken();
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
  }
  return config;
});

request.interceptors.response.use(
  (response) => {
    const data = response.data;
    if (data.code !== 200) {
      ElMessage.error(data.message || '请求失败');
      return Promise.reject(new Error(data.message || '请求失败'));
    }
    return data.data;
  },
  (error) => {
    if (error.response && error.response.status === 401) {
      removeToken();
      ElMessage.error('登录已过期，请重新登录');
    } else {
      ElMessage.error(error.message || '网络异常');
    }
    return Promise.reject(error);
  }
);

export default request;
