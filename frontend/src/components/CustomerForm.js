import React, { useState, useEffect } from 'react';
import DatePicker from 'react-datepicker';
import 'react-datepicker/dist/react-datepicker.css';
import api from '../services/api';

function CustomerForm({ customer, onSave, onCancel }) {
    const [formData, setFormData] = useState({
        name: '',
        dob: new Date(),
        nicNumber: '',
        mobiles: [''],
        addresses: [{
            addressLine1: '',
            addressLine2: '',
            city: { name: '' },
            country: { name: '' }
        }]
    });

    const [countries, setCountries] = useState([]);
    const [cities, setCities] = useState({});
    const [familyNic, setFamilyNic] = useState('');
    const [foundFamily, setFoundFamily] = useState(null);
    const [familyMembers, setFamilyMembers] = useState([]);
    const [familyIds, setFamilyIds] = useState([]);

    useEffect(() => {
        loadCountries();
        if (customer) {
            setFormData({
                name: customer.name || '',
                dob: customer.dob ? new Date(customer.dob) : new Date(),
                nicNumber: customer.nicNumber || '',
                mobiles: customer.mobiles?.map(m => m.mobileNumber) || [''],
                addresses: customer.addresses?.length > 0
                    ? customer.addresses.map(a => ({
                        addressLine1: a.addressLine1 || '',
                        addressLine2: a.addressLine2 || '',
                        city: { name: a.city || '' },
                        country: { name: a.country || '' }
                    }))
                    : [{
                        addressLine1: '',
                        addressLine2: '',
                        city: { name: '' },
                        country: { name: '' }
                    }]
            });
            if (customer.family) {
                setFamilyMembers(customer.family);
                setFamilyIds(customer.family.map(f => f.id));
            }
        }
    }, [customer]);

    const loadCountries = async () => {
        try {
            const response = await api.getCountries();
            setCountries(response.data);
        } catch (error) {
            console.error('Failed to load countries');
        }
    };

    const loadCities = async (countryId) => {
        try {
            const response = await api.getCitiesByCountry(countryId);
            setCities(prev => ({ ...prev, [countryId]: response.data }));
        } catch (error) {
            console.error('Failed to load cities');
        }
    };

    const handleChange = (e) => {
        setFormData({ ...formData, [e.target.name]: e.target.value });
    };

    const handleMobileChange = (index, value) => {
        const mobiles = [...formData.mobiles];
        mobiles[index] = value;
        setFormData({ ...formData, mobiles });
    };

    const addMobile = () => {
        setFormData({ ...formData, mobiles: [...formData.mobiles, ''] });
    };

    const removeMobile = (index) => {
        const mobiles = formData.mobiles.filter((_, i) => i !== index);
        setFormData({ ...formData, mobiles });
    };

    const handleAddressChange = (index, field, value) => {
        const addresses = [...formData.addresses];
        addresses[index][field] = value;
        setFormData({ ...formData, addresses });
    };

    const handleAddressCityChange = (index, cityName) => {
        const addresses = [...formData.addresses];
        addresses[index].city = { name: cityName };
        setFormData({ ...formData, addresses });
    };

    const handleAddressCountryChange = (index, countryName, countryId) => {
        const addresses = [...formData.addresses];
        addresses[index].country = { name: countryName };
        addresses[index].city = { name: '' };
        setFormData({ ...formData, addresses });
        loadCities(countryId);
    };

    const addAddress = () => {
        setFormData({
            ...formData,
            addresses: [...formData.addresses, {
                addressLine1: '',
                addressLine2: '',
                city: { name: '' },
                country: { name: '' }
            }]
        });
    };

    const removeAddress = (index) => {
        const addresses = formData.addresses.filter((_, i) => i !== index);
        setFormData({ ...formData, addresses });
    };

    const handleSearchFamily = async () => {
        if (!familyNic.trim()) {
            alert('Please enter NIC number');
            return;
        }
        try {
            const response = await api.searchByNic(familyNic.trim());
            setFoundFamily(response.data);
        } catch (error) {
            alert('Customer not found with NIC: ' + familyNic);
            setFoundFamily(null);
        }
    };

    const handleAddFamily = async () => {
        if (!foundFamily) return;

        if (familyIds.includes(foundFamily.id)) {
            alert('Already a family member');
            return;
        }

        if (customer) {
            try {
                await api.addFamilyMember(customer.id, foundFamily.id);
                setFamilyMembers([...familyMembers, foundFamily]);
                setFamilyIds([...familyIds, foundFamily.id]);
                setFoundFamily(null);
                setFamilyNic('');
                alert('Family member added!');
            } catch (error) {
                alert('Failed to add family member');
            }
        } else {
            setFamilyMembers([...familyMembers, foundFamily]);
            setFamilyIds([...familyIds, foundFamily.id]);
            setFoundFamily(null);
            setFamilyNic('');
        }
    };

    const removeFamilyMember = async (memberId) => {
        if (customer) {
            alert('Remove family member from Customer View page');
            return;
        }
        setFamilyMembers(familyMembers.filter(f => f.id !== memberId));
        setFamilyIds(familyIds.filter(id => id !== memberId));
    };

    const handleSubmit = async (e) => {
        e.preventDefault();

        if (!formData.name.trim()) {
            alert('Name is mandatory');
            return;
        }
        if (!formData.nicNumber.trim()) {
            alert('NIC Number is mandatory');
            return;
        }

        const payload = {
            name: formData.name,
            dob: formData.dob.toISOString().split('T')[0],
            nicNumber: formData.nicNumber,
            mobiles: formData.mobiles
                .filter(m => m.trim())
                .map(m => ({ mobileNumber: m })),
            addresses: formData.addresses
                .filter(a => a.addressLine1.trim() && a.country.name)
                .map(a => ({
                    addressLine1: a.addressLine1,
                    addressLine2: a.addressLine2,
                    city: { name: a.city.name },
                    country: { name: a.country.name }
                }))
        };

        onSave(payload, familyIds);
    };

    return (
        <div>
            <h3 className="mb-3">{customer ? 'Edit Customer' : 'Create Customer'}</h3>
            <form onSubmit={handleSubmit}>
                <div className="row mb-3">
                    <div className="col-md-6">
                        <label className="form-label">Name *</label>
                        <input
                            type="text"
                            className="form-control"
                            name="name"
                            value={formData.name}
                            onChange={handleChange}
                            required
                        />
                    </div>
                    <div className="col-md-6">
                        <label className="form-label">NIC Number *</label>
                        <input
                            type="text"
                            className="form-control"
                            name="nicNumber"
                            value={formData.nicNumber}
                            onChange={handleChange}
                            required
                        />
                    </div>
                </div>

                <div className="mb-3">
                    <label className="form-label">Date of Birth *</label>
                    <br/>
                    <DatePicker
                        selected={formData.dob}
                        onChange={(date) => setFormData({ ...formData, dob: date })}
                        className="form-control"
                        dateFormat="yyyy-MM-dd"
                        showYearDropdown
                        maxDate={new Date()}
                    />
                </div>

                <div className="mb-3">
                    <label className="form-label">Mobile Numbers</label>
                    {formData.mobiles.map((mobile, index) => (
                        <div key={index} className="input-group mb-2">
                            <input
                                type="text"
                                className="form-control"
                                value={mobile}
                                onChange={(e) => handleMobileChange(index, e.target.value)}
                                placeholder="Mobile number"
                            />
                            {formData.mobiles.length > 1 && (
                                <button
                                    type="button"
                                    className="btn btn-danger"
                                    onClick={() => removeMobile(index)}>
                                    ✕
                                </button>
                            )}
                        </div>
                    ))}
                    <button type="button" className="btn btn-sm btn-outline-primary" onClick={addMobile}>
                        + Add Mobile
                    </button>
                </div>

                <div className="mb-3">
                    <label className="form-label">Addresses</label>
                    {formData.addresses.map((address, index) => (
                        <div key={index} className="border p-3 mb-2 rounded">
                            <div className="row">
                                <div className="col-md-6 mb-2">
                                    <label className="form-label">Country</label>
                                    <select
                                        className="form-select"
                                        value={address.country.name}
                                        onChange={(e) => {
                                            const country = countries.find(c => c.name === e.target.value);
                                            handleAddressCountryChange(index, e.target.value, country?.id);
                                        }}>
                                        <option value="">Select Country</option>
                                        {countries.map(c => (
                                            <option key={c.id} value={c.name}>{c.name}</option>
                                        ))}
                                    </select>
                                </div>
                                <div className="col-md-6 mb-2">
                                    <label className="form-label">City</label>
                                    <select
                                        className="form-select"
                                        value={address.city.name}
                                        onChange={(e) => handleAddressCityChange(index, e.target.value)}>
                                        <option value="">Select City</option>
                                        {cities[countries.find(c => c.name === address.country.name)?.id]?.map(city => (
                                            <option key={city.id} value={city.name}>{city.name}</option>
                                        ))}
                                    </select>
                                </div>
                            </div>
                            <div className="mb-2">
                                <label className="form-label">Address Line 1</label>
                                <input
                                    type="text"
                                    className="form-control"
                                    value={address.addressLine1}
                                    onChange={(e) => handleAddressChange(index, 'addressLine1', e.target.value)}
                                    placeholder="Street address"
                                />
                            </div>
                            <div className="mb-2">
                                <label className="form-label">Address Line 2</label>
                                <input
                                    type="text"
                                    className="form-control"
                                    value={address.addressLine2}
                                    onChange={(e) => handleAddressChange(index, 'addressLine2', e.target.value)}
                                    placeholder="Apt, Suite, etc."
                                />
                            </div>
                            {formData.addresses.length > 1 && (
                                <button
                                    type="button"
                                    className="btn btn-sm btn-danger"
                                    onClick={() => removeAddress(index)}>
                                    Remove Address
                                </button>
                            )}
                        </div>
                    ))}
                    <button type="button" className="btn btn-sm btn-outline-primary" onClick={addAddress}>
                        + Add Address
                    </button>
                </div>

                {/* Family Members Section */}
                <div className="mb-4 p-3 border rounded bg-light">
                    <h5>Family Members</h5>

                    <div className="row mb-3">
                        <div className="col-md-8">
                            <label className="form-label">Search by NIC</label>
                            <div className="input-group">
                                <input
                                    type="text"
                                    className="form-control"
                                    placeholder="Enter NIC to search"
                                    value={familyNic}
                                    onChange={(e) => setFamilyNic(e.target.value)}
                                    onKeyDown={(e) => e.key === 'Enter' && (e.preventDefault(), handleSearchFamily())}
                                />
                                <button
                                    className="btn btn-outline-primary"
                                    type="button"
                                    onClick={handleSearchFamily}>
                                    Search
                                </button>
                            </div>
                        </div>
                    </div>

                    {foundFamily && (
                        <div className="alert alert-success d-flex justify-content-between align-items-center">
                            <span>
                                <strong>{foundFamily.name}</strong> - NIC: {foundFamily.nicNumber}
                            </span>
                            <button
                                className="btn btn-sm btn-success"
                                type="button"
                                onClick={handleAddFamily}>
                                + Add as Family
                            </button>
                        </div>
                    )}

                    {familyMembers.length > 0 && (
                        <div className="mt-2">
                            <label className="form-label">Family Members:</label>
                            <ul className="list-group">
                                {familyMembers.map(member => (
                                    <li key={member.id} className="list-group-item d-flex justify-content-between align-items-center">
                                        <span>
                                            <strong>{member.name}</strong> - NIC: {member.nicNumber}
                                        </span>
                                        <button
                                            type="button"
                                            className="btn btn-sm btn-outline-danger"
                                            onClick={() => removeFamilyMember(member.id)}>
                                            ✕
                                        </button>
                                    </li>
                                ))}
                            </ul>
                        </div>
                    )}
                </div>

                <div className="d-flex gap-2">
                    <button type="submit" className="btn btn-success">
                        {customer ? 'Update Customer' : 'Create Customer'}
                    </button>
                    <button type="button" className="btn btn-secondary" onClick={onCancel}>
                        Cancel
                    </button>
                </div>
            </form>
        </div>
    );
}

export default CustomerForm;