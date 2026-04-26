import React, { useState, useEffect } from 'react';
import api from '../services/api';

function CustomerTable({ onEdit, onView, refreshTrigger }) {
    const [customers, setCustomers] = useState([]);
    const [loading, setLoading] = useState(false);
    const [page, setPage] = useState(0);
    const [totalPages, setTotalPages] = useState(0);

    useEffect(() => {
        loadCustomers();
    }, [page, refreshTrigger]);

    const loadCustomers = async () => {
        setLoading(true);
        try {
            const response = await api.getAllCustomers(page);
            setCustomers(response.data.content);
            setTotalPages(response.data.totalPages);
        } catch (error) {
            alert('Failed to load customers: ' + error.message);
        }
        setLoading(false);
    };

    const handleDelete = async (id) => {
        if (window.confirm('Are you sure you want to delete this customer?')) {
            try {
                await api.deleteCustomer(id);
                loadCustomers();
            } catch (error) {
                alert('Delete failed: ' + error.message);
            }
        }
    };

    if (loading) return <div className="text-center py-4">Loading...</div>;

    return (
        <div>
            <h3 className="mb-3">Customer List</h3>
            <div className="table-responsive">
                <table className="table table-striped table-hover">
                    <thead className="table-dark">
                        <tr>
                            <th>ID</th>
                            <th>Name</th>
                            <th>Date of Birth</th>
                            <th>NIC Number</th>
                            <th>Mobiles</th>
                            <th>Addresses</th>
                            <th>Family Members</th>
                            <th>Actions</th>
                        </tr>
                    </thead>
                    <tbody>
                        {customers.length === 0 ? (
                            <tr>
                                <td colSpan="8" className="text-center">No customers found</td>
                            </tr>
                        ) : (
                            customers.map(customer => (
                                <tr key={customer.id}>
                                    <td>{customer.id}</td>
                                    <td>{customer.name}</td>
                                    <td>{new Date(customer.dob).toLocaleDateString()}</td>
                                    <td>{customer.nicNumber}</td>
                                    <td>
                                        {customer.mobiles?.map(m => m.mobileNumber).join(', ') || '-'}
                                    </td>
                                    <td>
                                        {customer.addresses?.length || 0} address(es)
                                    </td>
                                    <td>
                                        {customer.family?.length || 0} member(s)
                                    </td>
                                    <td>
                                        <button 
                                            className="btn btn-sm btn-info me-1"
                                            onClick={() => onView(customer)}>
                                            View
                                        </button>
                                        <button 
                                            className="btn btn-sm btn-warning me-1"
                                            onClick={() => onEdit(customer)}>
                                            Edit
                                        </button>
                                        <button 
                                            className="btn btn-sm btn-danger"
                                            onClick={() => handleDelete(customer.id)}>
                                            Delete
                                        </button>
                                    </td>
                                </tr>
                            ))
                        )}
                    </tbody>
                </table>
            </div>

            {/* Pagination */}
            <div className="d-flex justify-content-center gap-2 mt-3">
                <button 
                    className="btn btn-outline-primary"
                    disabled={page === 0}
                    onClick={() => setPage(page - 1)}>
                    Previous
                </button>
                <span className="align-self-center">Page {page + 1} of {totalPages}</span>
                <button 
                    className="btn btn-outline-primary"
                    disabled={page >= totalPages - 1}
                    onClick={() => setPage(page + 1)}>
                    Next
                </button>
            </div>
        </div>
    );
}

export default CustomerTable;