import React, { useState, useCallback } from 'react';
import 'bootstrap/dist/css/bootstrap.min.css';
import CustomerTable from './components/CustomerTable';
import CustomerForm from './components/CustomerForm';
import CustomerView from './components/CustomerView';
import BulkUpload from './components/BulkUpload';
import api from './services/api';
import './App.css';

function App() {
    const [view, setView] = useState('table');
    const [selectedCustomer, setSelectedCustomer] = useState(null);
    const [refreshTrigger, setRefreshTrigger] = useState(0);

    const triggerRefresh = useCallback(() => {
        setRefreshTrigger(prev => prev + 1);
    }, []);

    const handleCreate = () => {
        setSelectedCustomer(null);
        setView('create');
    };

    const handleEdit = (customer) => {
        setSelectedCustomer(customer);
        setView('edit');
    };

    const handleView = (customer) => {
        setSelectedCustomer(customer);
        setView('viewCustomer');
    };

    const handleSave = async (payload, familyIds) => {
        try {
            let savedCustomer;
            if (view === 'edit' && selectedCustomer) {
                const response = await api.updateCustomer(selectedCustomer.id, payload);
                savedCustomer = response.data;
            } else {
                const response = await api.createCustomer(payload);
                savedCustomer = response.data;
            }

            if (familyIds && familyIds.length > 0 && savedCustomer) {
                for (const familyId of familyIds) {
                    try {
                        await api.addFamilyMember(savedCustomer.id, familyId);
                    } catch (err) {
                        console.error('Failed to add family member:', familyId);
                    }
                }
            }

            alert('Customer saved successfully!');
            setView('table');
            triggerRefresh();
        } catch (error) {
            alert('Operation failed: ' + (error.response?.data?.message || error.message));
        }
    };

    const handleCancel = () => {
        setView('table');
        setSelectedCustomer(null);
    };

    return (
        <div className="container-fluid py-4">
            <h1 className="text-center mb-4">Customer Management System</h1>

            <div className="d-flex justify-content-center gap-2 mb-4">
                <button
                    className={`btn ${view === 'table' ? 'btn-primary' : 'btn-outline-primary'}`}
                    onClick={() => setView('table')}>
                    View All Customers
                </button>
                <button
                    className={`btn ${view === 'create' ? 'btn-success' : 'btn-outline-success'}`}
                    onClick={handleCreate}>
                    Create Customer
                </button>
                <button
                    className={`btn ${view === 'bulk' ? 'btn-warning' : 'btn-outline-warning'}`}
                    onClick={() => setView('bulk')}>
                    Bulk Upload
                </button>
            </div>

            <div className="row justify-content-center">
                <div className="col-12">
                    {view === 'table' && (
                        <CustomerTable
                            onEdit={handleEdit}
                            onView={handleView}
                            refreshTrigger={refreshTrigger}
                        />
                    )}

                    {(view === 'create' || view === 'edit') && (
                        <div className="row justify-content-center">
                            <div className="col-md-8">
                                <CustomerForm
                                    customer={view === 'edit' ? selectedCustomer : null}
                                    onSave={handleSave}
                                    onCancel={handleCancel}
                                />
                            </div>
                        </div>
                    )}

                    {view === 'viewCustomer' && (
                        <div className="row justify-content-center">
                            <div className="col-md-8">
                                <CustomerView
                                    customer={selectedCustomer}
                                    onClose={handleCancel}
                                />
                            </div>
                        </div>
                    )}

                    {view === 'bulk' && (
                        <div className="row justify-content-center">
                            <div className="col-md-8">
                                <BulkUpload />
                            </div>
                        </div>
                    )}
                </div>
            </div>
        </div>
    );
}

export default App;