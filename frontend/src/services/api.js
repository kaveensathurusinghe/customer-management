import axios from 'axios';

const API_BASE_URL = 'http://localhost:8080/api';

const api = {
    createCustomer: (customer) => axios.post(`${API_BASE_URL}/customers`, customer),
    updateCustomer: (id, customer) => axios.put(`${API_BASE_URL}/customers/${id}`, customer),
    getCustomer: (id) => axios.get(`${API_BASE_URL}/customers/${id}`),
    getAllCustomers: (page = 0, size = 50) => 
        axios.get(`${API_BASE_URL}/customers?page=${page}&size=${size}&sortBy=name&direction=asc`),
    deleteCustomer: (id) => axios.delete(`${API_BASE_URL}/customers/${id}`),
    searchByNic: (nic) => axios.get(`${API_BASE_URL}/customers/nic/${nic}`),
 
    addMobile: (customerId, mobileNumber) => 
        axios.post(`${API_BASE_URL}/customers/${customerId}/mobiles`, { mobileNumber }),

    addAddress: (customerId, address) => 
        axios.post(`${API_BASE_URL}/customers/${customerId}/addresses`, address),

    addFamilyMember: (customerId, familyMemberId) => 
        axios.post(`${API_BASE_URL}/customers/${customerId}/family/${familyMemberId}`),

    bulkUpload: (file) => {
        const formData = new FormData();
        formData.append('file', file);
        return axios.post(`${API_BASE_URL}/customers/bulk`, formData, {
            headers: { 'Content-Type': 'multipart/form-data' },
            timeout: 300000 
        });
    },

    getCountries: () => axios.get(`${API_BASE_URL}/countries`),
    getCitiesByCountry: (countryId) => axios.get(`${API_BASE_URL}/cities/by-country/${countryId}`)
};

export default api;