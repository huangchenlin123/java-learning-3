import request from './request'

export const posSale = (data) => request.post('/sale/order/pos', data)

export const pageSaleOrder = (params) => request.get('/sale/order', { params })
export const getSaleOrder = (id) => request.get(`/sale/order/${id}`)
export const approveSaleOrder = (id, approved) => request.put(`/sale/order/${id}/approve`, { approved })

export const pageCustomer = (params) => request.get('/sale/customer', { params })
export const saveCustomer = (data) => request.post('/sale/customer', data)
export const updateCustomer = (data) => request.put(`/sale/customer/${data.id}`, data)

export const pagePromotion = (params) => request.get('/promotion', { params })
export const getPromotion = (id) => request.get(`/promotion/${id}`)
export const savePromotion = (data) => request.post('/promotion', data)
export const updatePromotion = (data) => request.put(`/promotion/${data.id}`, data)
export const deletePromotion = (id) => request.delete(`/promotion/${id}`)
export const previewPromotion = (data) => request.post('/promotion/preview', data)
