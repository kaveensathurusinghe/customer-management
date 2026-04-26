import React from 'react';

function CustomerView({ customer, onClose }) {
    if (!customer) return null;

    return (
        <div>
            <h3 className="mb-3">Customer Details</h3>
            <div className="card">
                <div className="card-body">
                    <h5 className="card-title">{customer.name}</h5>
                    
                    <table className="table table-bordered mt-3">
                        <tbody>
                            <tr>
                                <th width="30%">ID</th>
                                <td>{customer.id}</td>
                            </tr>
                            <tr>
                                <th>Name</th>
                                <td>{customer.name}</td>
                            </tr>
                            <tr>
                                <th>Date of Birth</th>
                                <td>{new Date(customer.dob).toLocaleDateString()}</td>
                            </tr>
                            <tr>
                                <th>NIC Number</th>
                                <td>{customer.nicNumber}</td>
                            </tr>
                            <tr>
                                <th>Mobile Numbers</th>
                                <td>
                                    {customer.mobiles?.length > 0 
                                        ? customer.mobiles.map(m => m.mobileNumber).join(', ')
                                        : 'N/A'}
                                </td>
                            </tr>
                            <tr>
                                <th>Addresses</th>
                                <td>
                                    {customer.addresses?.length > 0 ? (
                                        <ul className="list-unstyled mb-0">
                                            {customer.addresses.map((addr, i) => (
                                                <li key={i}>
                                                    {addr.addressLine1}
                                                    {addr.addressLine2 && `, ${addr.addressLine2}`}
                                                    , {addr.city?.name}, {addr.country?.name}
                                                </li>
                                            ))}
                                        </ul>
                                    ) : 'N/A'}
                                </td>
                            </tr>
                            <tr>
                                <th>Family Members</th>
                                <td>
                                    {customer.family?.length > 0 
                                        ? customer.family.map(f => f.name).join(', ')
                                        : 'None'}
                                </td>
                            </tr>
                            <tr>
                                <th>Created</th>
                                <td>{new Date(customer.createdAt).toLocaleString()}</td>
                            </tr>
                            <tr>
                                <th>Updated</th>
                                <td>{new Date(customer.updatedAt).toLocaleString()}</td>
                            </tr>
                        </tbody>
                    </table>

                    <button className="btn btn-secondary" onClick={onClose}>Close</button>
                </div>
            </div>
        </div>
    );
}

export default CustomerView;