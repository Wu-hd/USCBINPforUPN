import { http } from './http';
export const loginApi = (payload) => {
    return http.post('/api/auth/login', payload);
};
export const meApi = () => {
    return http.get('/api/auth/me');
};
export const menusApi = () => {
    return http.get('/api/auth/menus');
};
export const healthApi = () => {
    return http.get('/api/health');
};
//# sourceMappingURL=auth.js.map