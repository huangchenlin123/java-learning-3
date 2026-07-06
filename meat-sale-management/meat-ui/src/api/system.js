import request from './request'

export const pageUser = (params) => request.get('/system/user', { params })
export const saveUser = (data) => request.post('/system/user', data)
export const updateUser = (data) => request.put(`/system/user/${data.id}`, data)
export const deleteUser = (id) => request.delete(`/system/user/${id}`)

export const listRole = () => request.get('/system/role')
export const saveRole = (data) => request.post('/system/role', data)
export const updateRole = (data) => request.put(`/system/role/${data.id}`, data)
export const deleteRole = (id) => request.delete(`/system/role/${id}`)

export const listMenu = () => request.get('/system/menu')
export const saveMenu = (data) => request.post('/system/menu', data)
export const updateMenu = (data) => request.put(`/system/menu/${data.id}`, data)
export const deleteMenu = (id) => request.delete(`/system/menu/${id}`)

export const listDict = () => request.get('/system/dict')
export const saveDict = (data) => request.post('/system/dict', data)
export const updateDict = (data) => request.put(`/system/dict/${data.id}`, data)
export const deleteDict = (id) => request.delete(`/system/dict/${id}`)
