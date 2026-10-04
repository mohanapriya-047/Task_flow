import client from './client'

export const register = (payload) => client.post('/auth/register', payload)
export const verifyRegistrationOtp = (payload) => client.post('/auth/verify-registration-otp', payload)
export const login = (payload) => client.post('/auth/login', payload)
export const verifyLoginOtp = (payload) => client.post('/auth/verify-login-otp', payload)
export const resendOtp = (payload, type) => client.post(`/auth/resend-otp?type=${type}`, payload)
export const logout = () => client.post('/auth/logout')

export const sendManagerPhoneOtp = () => client.post('/auth/manager-phone-otp')
export const verifyManagerPhoneOtp = (otp) => client.post('/auth/verify-manager-phone-otp', { otp })

export const requestAdminAuthorization = (operationDescription) =>
  client.post('/admin/authorization/request', { operationDescription })
export const verifyAdminAuthEmail = (authorizationToken, otp) =>
  client.post('/admin/authorization/verify-email', { authorizationToken, otp })
export const verifyAdminAuthPhone = (authorizationToken, otp) =>
  client.post('/admin/authorization/verify-phone', { authorizationToken, otp })
