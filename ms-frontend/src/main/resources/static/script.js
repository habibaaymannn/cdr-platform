document.addEventListener('DOMContentLoaded', async () => {
    // Keycloak initialization
    const keycloak = new Keycloak({
        url: 'http://localhost:8081',
        realm: 'cdr-realm',
        clientId: 'ms-frontend'
    });
    const frontendBase = window.location.origin; // e.g. http://localhost:8083

    let token = '';
    let cdrs = [];

    // DOM Elements
    const loginBtn = document.getElementById('loginBtn');
    const logoutBtn = document.getElementById('logoutBtn');
    const fetchCdrBtn = document.getElementById('fetchCdrBtn');
    const reportForm = document.getElementById('reportForm');
    const searchForm = document.getElementById('searchForm');
    const sortForm = document.getElementById('sortForm');
    const clearSearchBtn = document.getElementById('clearSearch');
    const cdrDataDiv = document.getElementById('cdr-data');
    const usageReportDiv = document.getElementById('usage-report');


    console.log('DOM Elements:', { loginBtn, logoutBtn, fetchCdrBtn, reportForm, searchForm, sortForm, clearSearchBtn, cdrDataDiv, usageReportDiv });
    console.log('Current URL:', window.location.href);
    console.log('URL Fragment:', window.location.hash);
    console.log('URL Search:', window.location.search);
    if (!loginBtn) console.error('Login button not found! Check id="loginBtn" in index.html');


    async function testKeycloakEndpoint() {
        try {
            const response = await fetch('http://localhost:8081/realms/cdr-realm/.well-known/openid-configuration', {
                method: 'GET',
                headers: { 'Accept': 'application/json' }
            });
            console.log('Keycloak endpoint response:', response.status);
            if (!response.ok) throw new Error(`HTTP error ${response.status}`);
            const config = await response.json();
            console.log('Keycloak configuration:', config);
            return true;
        } catch (err) {
            console.error('Keycloak endpoint test failed:', err);
            return false;
        }
    }

    //Keycloak initialization
    const keycloakAvailable = await testKeycloakEndpoint();
    if (!keycloakAvailable) {
        cdrDataDiv.innerHTML = 'Error: Keycloak server unavailable. Check http://localhost:8081.';
        loginBtn.disabled = true;
        return;
    }

    try {
        const authenticated = await keycloak.init({
            onLoad: 'login-required',
            checkLoginIframe: false,
            redirectUri: frontendBase + '/index.html',
            responseMode: 'fragment',
            flow: 'standard'
        });
        console.log('Authenticated:', authenticated);
        console.log('Token:', keycloak.token);
        console.log('Session ID:', keycloak.sessionId);
        console.log('Token Parsed:', keycloak.tokenParsed);
        console.log('Keycloak Auth Server URL:', keycloak.authServerUrl);
        console.log('Keycloak Redirect URI:', keycloak.redirectUri);
    } catch (err) {
        console.error('Keycloak initialization failed:', err);
        cdrDataDiv.innerHTML = `Failed to initialize authentication: ${err.message || 'Unknown error'}.`;
    }


    keycloak.onTokenExpired = () => console.log('Token expired');
    keycloak.onAuthSuccess = () => console.log('Auth success:', keycloak.token);
    keycloak.onAuthError = (errorData) => console.error('Auth error:', errorData);


    async function getValidToken() {
        try {
            if (!keycloak.authenticated) {
                console.warn('Not authenticated, please log in');
                cdrDataDiv.innerHTML = 'Not authenticated, please click "Login".';
                return null;
            }
            console.log('Checking token validity...');
            await keycloak.updateToken(30);
            token = keycloak.token;
            console.log('Token refreshed:', token.substring(0, 20) + '...');
            return token;
        } catch (err) {
            console.error('Token refresh failed:', err);
            cdrDataDiv.innerHTML = `Authentication error: ${err.message || 'Please log in again'}`;
            return null;
        }
    }


    function renderCdrTable(data) {
        if (data.length === 0) {
            cdrDataDiv.innerHTML = 'No CDR records found.';
            return;
        }

        const formattedData = data.map(cdr => ({
            id: cdr.id,
            source: cdr.source,
            destination: cdr.destination,
            startTime: cdr.startTime,
            service: cdr.service,
            usageAmount: cdr.usageAmount
        }));
        cdrDataDiv.innerHTML = `
            <table border="1">
                <tr>
                    <th>ID</th>
                    <th>Source</th>
                    <th>Destination</th>
                    <th>Start Time</th>
                    <th>Service</th>
                    <th>Usage</th>
                </tr>
                ${formattedData.map(cdr => `
                    <tr>
                        <td>${cdr.id}</td>
                        <td>${cdr.source}</td>
                        <td>${cdr.destination}</td>
                        <td>${new Date(cdr.startTime).toLocaleString()}</td>
                        <td>${cdr.service}</td>
                        <td>${cdr.usageAmount}</td>
                    </tr>
                `).join('')}
            </table>
        `;
    }


    function searchCdrs(data, query) {
        if (!query) return data;
        const lowerQuery = query.toLowerCase();
        return data.filter(cdr =>
            cdr.id.toString().includes(lowerQuery) ||
            cdr.source.toLowerCase().includes(lowerQuery) ||
            cdr.destination.toLowerCase().includes(lowerQuery) ||
            new Date(cdr.startTime).toLocaleString().toLowerCase().includes(lowerQuery) ||
            cdr.service.toLowerCase().includes(lowerQuery) ||
            cdr.usageAmount.toString().includes(lowerQuery)
        );
    }


    function sortCdrs(data, field, direction) {
        const sorted = [...data];
        sorted.sort((a, b) => {
            let valA = a[field];
            let valB = b[field];
            if (field === 'startTime') {
                valA = new Date(valA).getTime();
                valB = new Date(valB).getTime();
            } else if (field === 'usageAmount') {
                valA = parseFloat(valA);
                valB = parseFloat(valB);
            }
            if (valA < valB) return direction === 'asc' ? -1 : 1;
            if (valA > valB) return direction === 'asc' ? 1 : -1;
            return 0;
        });
        return sorted;
    }


    function filterCdrsByDate(data, fromDate) {
        let filtered = data;
        if (fromDate) {
            const start = new Date(fromDate).setHours(0, 0, 0, 0);
            filtered = filtered.filter(cdr => new Date(cdr.startTime).getTime() >= start);
        }
        // No toDate -> include all records up to today
        const today = new Date().setHours(23, 59, 59, 999);
        filtered = filtered.filter(cdr => new Date(cdr.startTime).getTime() <= today);
        return filtered;
    }


    function generateReport(type, data) {
        if (data.length === 0) {
            return '<p>No data available for the selected date range.</p>';
        }
        if (type === 'dailyCount') {
            const dailyCounts = data.reduce((acc, cdr) => {
                const date = new Date(cdr.startTime).toISOString().split('T')[0];
                acc[date] = (acc[date] || 0) + 1;
                return acc;
            }, {});
            return `
                <h3>Daily Usage Count</h3>
                <table border="1">
                    <tr><th>Date</th><th>CDR Count</th></tr>
                    ${Object.entries(dailyCounts).map(([date, count]) => `
                        <tr><td>${date}</td><td>${count}</td></tr>
                    `).join('')}
                </table>
            `;
        } else if (type === 'dailyService') {
            const dailyServiceUsage = data.reduce((acc, cdr) => {
                const date = new Date(cdr.startTime).toISOString().split('T')[0];
                acc[date] = acc[date] || { VOICE: 0, SMS: 0, DATA: 0 };
                acc[date][cdr.service] += cdr.usageAmount;
                return acc;
            }, {});
            return `
                <h3>Daily Usage per Service</h3>
                <table border="1">
                    <tr><th>Date</th><th>VOICE (minutes)</th><th>SMS (messages)</th><th>DATA (MB)</th></tr>
                    ${Object.entries(dailyServiceUsage).map(([date, usage]) => `
                        <tr>
                            <td>${date}</td>
                            <td>${usage.VOICE.toFixed(2)}</td>
                            <td>${usage.SMS.toFixed(2)}</td>
                            <td>${usage.DATA.toFixed(2)}</td>
                        </tr>
                    `).join('')}
                </table>
            `;
        } else if (type === 'topSources') {
            const sourceCounts = data.reduce((acc, cdr) => {
                acc[cdr.source] = (acc[cdr.source] || 0) + 1;
                return acc;
            }, {});
            const topSources = Object.entries(sourceCounts)
                .sort((a, b) => b[1] - a[1])
                .slice(0, 5);
            return `
                <h3>Top 5 Sources by CDR Count</h3>
                <table border="1">
                    <tr><th>Source</th><th>CDR Count</th></tr>
                    ${topSources.map(([source, count]) => `
                        <tr><td>${source}</td><td>${count}</td></tr>
                    `).join('')}
                </table>
            `;
        }
        return '<p>Invalid report type.</p>';
    }

    // Event Listeners
    if (loginBtn) {
        loginBtn.addEventListener('click', () => {
            console.log('Login button clicked');
            try {
                keycloak.login({ redirectUri: window.location.origin + '/Call_Detail_Records/static/index.html' });
                console.log('Initiating Keycloak login...');
            } catch (err) {
                console.error('Login failed:', err);
                cdrDataDiv.innerHTML = `Login error: ${err.message || 'Unable to initiate login'}`;
            }
        });
    } else {
        console.error('Cannot attach loginBtn listener: element not found');
    }

    if (logoutBtn) {
        logoutBtn.addEventListener('click', () => {
            console.log('Logout button clicked');
            keycloak.logout({ redirectUri: window.location.origin + '/index.html' });
        });
    } else {
        console.error('logoutBtn not found');
    }

    if (fetchCdrBtn) {
        fetchCdrBtn.addEventListener('click', async () => {
            console.log('Fetch CDR button clicked');
            cdrDataDiv.innerHTML = 'Loading...';
            try {
                const validToken = await getValidToken();
                if (!validToken) return;
                console.log('Fetching CDRs with token:', validToken.substring(0, 20) + '...');
                const response = await fetch('http://localhost:8080/cdrs', {
                    method: 'GET',
                    headers: {
                        'Authorization': `Bearer ${validToken}`,
                        'Content-Type': 'application/json'
                    }
                });
                console.log('Response status:', response.status);
                if (!response.ok) {
                    const errorText = await response.text();
                    throw new Error(`HTTP error ${response.status}: ${errorText || response.statusText}`);
                }
                cdrs = await response.json();
                console.log('CDR Records:', cdrs);
                renderCdrTable(cdrs);
            } catch (err) {
                console.error('Error fetching CDRs:', err);
                cdrDataDiv.innerHTML = `Error: ${err.message}`;
            }
        });
    } else {
        console.error('fetchCdrBtn not found');
    }

    if (searchForm) {
        searchForm.addEventListener('submit', (e) => {
            e.preventDefault();
            console.log('Search form submitted');
            const searchQuery = document.getElementById('searchQuery').value;
            console.log(`Searching for: ${searchQuery}`);
            const filteredCdrs = searchCdrs(cdrs, searchQuery);
            renderCdrTable(filteredCdrs);
        });
    } else {
        console.error('searchForm not found');
    }

    if (clearSearchBtn) {
        clearSearchBtn.addEventListener('click', () => {
            console.log('Clear search clicked');
            document.getElementById('searchQuery').value = '';
            renderCdrTable(cdrs);
        });
    } else {
        console.error('clearSearchBtn not found');
    }

    if (sortForm) {
        sortForm.addEventListener('submit', (e) => {
            e.preventDefault();
            console.log('Sort form submitted');
            const sortField = document.getElementById('sortField').value;
            const sortDirection = document.getElementById('sortDirection').value;
            console.log(`Sorting by ${sortField} in ${sortDirection} order`);
            const searchQuery = document.getElementById('searchQuery').value;
            let filteredCdrs = searchCdrs(cdrs, searchQuery); // Apply search first
            filteredCdrs = sortCdrs(filteredCdrs, sortField, sortDirection);
            renderCdrTable(filteredCdrs);
        });
    } else {
        console.error('sortForm not found');
    }

    if (reportForm) {
        reportForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            console.log('Report form submitted');
            usageReportDiv.innerHTML = 'Loading...';
            try {
                const validToken = await getValidToken();
                if (!validToken) return;
                console.log('Fetching CDRs for report with token:', validToken.substring(0, 20) + '...');
                const response = await fetch('http://localhost:8080/cdrs', {
                    method: 'GET',
                    headers: {
                        'Authorization': `Bearer ${validToken}`,
                        'Content-Type': 'application/json'
                    }
                });
                console.log('Response status:', response.status);
                if (!response.ok) {
                    const errorText = await response.text();
                    throw new Error(`HTTP error ${response.status}: ${errorText || response.statusText}`);
                }
                const data = await response.json();
                console.log('CDR Data for Report:', data);

                const reportType = document.getElementById('reportType').value;
                const fromDate = document.getElementById('fromDate').value;
                console.log(`Generating ${reportType} report from ${fromDate || 'all dates'}`);

                const filteredData = filterCdrsByDate(data, fromDate);
                const reportHtml = generateReport(reportType, filteredData);
                usageReportDiv.innerHTML = reportHtml;
            } catch (err) {
                console.error('Error generating report:', err);
                usageReportDiv.innerHTML = `Error: ${err.message}`;
            }
        });
    } else {
        console.error('reportForm not found');
    }
});