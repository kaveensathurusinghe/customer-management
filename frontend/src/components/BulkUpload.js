import React, { useState } from 'react';
import api from '../services/api';

function BulkUpload() {
    const [file, setFile] = useState(null);
    const [uploading, setUploading] = useState(false);
    const [result, setResult] = useState(null);

    const handleFileChange = (e) => {
        setFile(e.target.files[0]);
        setResult(null);
    };

    const handleUpload = async () => {
        if (!file) {
            alert('Please select a file');
            return;
        }

        const allowedTypes = [
            'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
            'application/vnd.ms-excel'
        ];
        if (!allowedTypes.includes(file.type)) {
            alert('Please upload an Excel file (.xlsx or .xls)');
            return;
        }

        setUploading(true);
        setResult(null);
        
        try {
            const response = await api.bulkUpload(file);
            setResult(response.data);
        } catch (error) {
            alert('Upload failed: ' + error.message);
        }
        setUploading(false);
    };

    return (
        <div>
            <h3 className="mb-3">Bulk Customer Upload</h3>
            <div className="card p-4">
                <div className="mb-3">
                    <label className="form-label">Select Excel File</label>
                    <input
                        type="file"
                        className="form-control"
                        accept=".xlsx,.xls"
                        onChange={handleFileChange}
                    />
                    <small className="text-muted">
                        File format: Name | Date of Birth (yyyy-MM-dd) | NIC Number
                    </small>
                </div>

                <button
                    className="btn btn-primary"
                    onClick={handleUpload}
                    disabled={!file || uploading}>
                    {uploading ? 'Uploading...' : 'Upload'}
                </button>

                {result && (
                    <div className="mt-4">
                        <div className={`alert ${result.errorCount === 0 ? 'alert-success' : 'alert-warning'}`}>
                            <strong>Upload Complete!</strong><br/>
                            Success: {result.successCount}<br/>
                            Errors: {result.errorCount}
                        </div>
                        
                        {result.errors?.length > 0 && (
                            <div className="mt-2">
                                <h6>Errors:</h6>
                                <ul className="list-group">
                                    {result.errors.map((error, index) => (
                                        <li key={index} className="list-group-item list-group-item-danger">
                                            {error}
                                        </li>
                                    ))}
                                </ul>
                            </div>
                        )}
                    </div>
                )}
            </div>
        </div>
    );
}

export default BulkUpload;