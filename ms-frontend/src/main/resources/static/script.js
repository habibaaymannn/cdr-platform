document.addEventListener('DOMContentLoaded', async () => {
    const keycloak = new Keycloak({
        url: 'http://keycloak:8080',
        realm: 'cdr-realm',
        clientId: 'ms-frontend'
    });

    let token = '';
    let username = '';
    const cdrDataDiv = document.getElementById('cdr-data');
    const usageReportDiv = document.getElementById('usage-report');

    const loginBtn = document.getElementById('loginBtn');
    const logoutBtn = document.getElementById('logoutBtn');
    const fetchCdrBtn = document.getElementById('fetchCdrBtn');
    const reportForm = document.getElementById('reportForm');
    const searchForm = document.getElementById('searchForm');
    const sortForm = document.getElementById('sortForm');
    const clearSearchBtn = document.getElementById('clearSearch');

    let cdrs = [];

    keycloak.init({ onLoad: 'login-required' }).then(authenticated => {
        if (authenticated) {
            token = keycloak.token;
            username = keycloak.tokenParsed.preferred_username;
            alert(`Logged in as ${username}`);
        } else {
            cdrDataDiv.innerHTML = 'Not authenticated.';
        }

        // Automatically refresh token every 60 seconds
        setInterval(() => {
            keycloak.updateToken(60).then(refreshed => {
                if (refreshed) {
                    token = keycloak.token;
                    console.log('Token refreshed');
                }
            }).catch(() => {
                console.error('Failed to refresh token');
            });
        }, 60000);
    }).catch(() => {
        cdrDataDiv.innerHTML = 'Failed to initialize Keycloak.';
    });

    //login button
    if (loginBtn) {
        loginBtn.addEventListener('click', () => {
            keycloak.login();
        });
    }

    //logout button
    if (logoutBtn) {
        logoutBtn.addEventListener('click', () => {
            keycloak.logout();
            cdrDataDiv.innerHTML = 'Logged out.';
        });
    }

    async function getValidToken() {
        try {
            const refreshed = await keycloak.updateToken(60);
            if (refreshed) {
                token = keycloak.token;
                console.log('Token refreshed');
            }
            return token;
        } catch {
            cdrDataDiv.innerHTML = 'Session expired. Please login again.';
            return null;
        }
    }
    //fetch button
    if (fetchCdrBtn) {
        fetchCdrBtn.addEventListener('click', async () => {
            cdrDataDiv.innerHTML = 'Loading...';
            try {
                const validToken = await getValidToken();
                if (!validToken) return;
                const response = await fetch('http://localhost:8084/cdrs', {
                    method: 'GET',
                    headers: {
                        'Authorization': `Bearer ${validToken}`,
                        'Content-Type': 'application/json'
                    }
                });
                if (!response.ok) throw new Error(`HTTP ${response.status}`);
                cdrs = await response.json();
                renderCdrTable(cdrs);
            } catch (err) {
                cdrDataDiv.innerHTML = `Error: ${err.message}`;
            }
        });
    }
    dayjs.extend(dayjs_plugin_customParseFormat);
    dayjs.extend(dayjs_plugin_isBetween);

    function formatDate(dateString) {
        const parsed = dayjs(dateString, "DD-MM-YYYY HH:mm:ss");
        if (!parsed.isValid()) return "Invalid Date";
        return parsed.format("YYYY-MM-DD HH:mm:ss");
    }


    function renderCdrTable(data) {
        if (data.length === 0) {
            cdrDataDiv.innerHTML = 'No CDR records found.';
            return;
        }

        cdrDataDiv.innerHTML = `
      <table border="1">
        <tr>
          <th>ID</th>
          <th>Source</th>
          <th>Destination</th>
          <th>Start Time</th>
          <th>Service</th>
          <th>Usage Amout</th>
        </tr>
        ${data.map(cdr => `
          <tr>
            <td>${cdr.id}</td>
            <td>${cdr.source}</td>
            <td>${cdr.destination}</td>
            <td>${formatDate(cdr.startTime)}</td>
            <td>${cdr.service}</td>
            <td>${cdr.usageAmount}</td>
          </tr>
        `).join('')}
      </table>
    `;
    }
    function searchCdrs(data, query, field) {
        if (!query) return data;
        const lowerQuery = query.toLowerCase();

        return data.filter(cdr => {
            const parsedDate = dayjs(cdr.startTime, "DD-MM-YYYY HH:mm:ss");
            const dateString = parsedDate.isValid() ? parsedDate.format("YYYY-MM-DD HH:mm:ss") : "";

            switch (field) {
                case 'id':
                    return cdr.id.toString() === lowerQuery;
                case 'source':
                    return cdr.source.toLowerCase().includes(lowerQuery);
                case 'destination':
                    return cdr.destination.toLowerCase().includes(lowerQuery);
                case 'startTime':
                    return dateString.toLowerCase().includes(lowerQuery);
                case 'service':
                    return cdr.service.toLowerCase().includes(lowerQuery);
                case 'usageAmount':
                    return parseFloat(cdr.usageAmount) === parseFloat(query);
                case 'all':
                default:
                    return (
                        cdr.id.toString().includes(lowerQuery) ||
                        cdr.source.toLowerCase().includes(lowerQuery) ||
                        cdr.destination.toLowerCase().includes(lowerQuery) ||
                        dateString.toLowerCase().includes(lowerQuery) ||
                        cdr.service.toLowerCase().includes(lowerQuery) ||
                        cdr.usageAmount.toString().includes(lowerQuery)
                    );
            }
        });
    }

    function sortCdrs(data, field, direction) {
        const sorted = [...data];
        sorted.sort((a, b) => {
            let valA = a[field], valB = b[field];
            if (field === 'startTime') {
                valA = new Date(valA).getTime();
                valB = new Date(valB).getTime();
            } else if (field === 'usageAmount') {
                valA = parseFloat(valA);
                valB = parseFloat(valB);
            }
            return direction === 'asc' ? valA - valB : valB - valA;
        });
        return sorted;
    }

    function filterCdrsByDate(data, fromDate) {
        if (!fromDate) return data;

        //just for debugging
        const start = dayjs(fromDate, "YYYY-MM-DD").startOf('day');
        const end = dayjs(fromDate, "YYYY-MM-DD").endOf('day');

        console.log('Parsed fromDate:', start.format());
        console.log('End of that day:', end.format());

        return data.filter(cdr => {
            const cdrDate = dayjs(cdr.startTime, "DD-MM-YYYY HH:mm:ss");
            const isValid = cdrDate.isValid();
            const inRange = isValid && cdrDate.isBetween(start, end, null, '[]');
            console.log('CDR:', cdr.startTime, 'Parsed:', cdrDate.format(), 'Valid:', isValid, 'In Range:', inRange);
            return inRange;
        });
    }



    function generateReport(type, data) {
        if (data.length === 0) return '<p>No data available.</p>';

        if (type === 'dailyCount') {
            const dailyCounts = data.reduce((acc, cdr) => {
                const parsed = dayjs(cdr.startTime, "DD-MM-YYYY HH:mm:ss");
                const date = parsed.isValid() ? parsed.format("YYYY-MM-DD") : "Invalid";
                if (date !== "Invalid") {
                    acc[date] = (acc[date] || 0) + 1;
                }
                return acc;
            }, {});
            return `
        <h3>Daily Usage Count</h3>
        <table border="1">
            <tr><th>Date</th><th>CDR Count</th></tr>
            ${Object.entries(dailyCounts).map(([date, count]) =>
                `<tr><td>${date}</td><td>${count}</td></tr>`).join('')}
        </table>
    `;
        }
        else if (type === 'dailyService') {
            const dailyService = data.reduce((acc, cdr) => {
                const parsed = dayjs(cdr.startTime, "DD-MM-YYYY HH:mm:ss");
                const date = parsed.isValid() ? parsed.format("YYYY-MM-DD") : "Invalid";
                acc[date] = acc[date] || { VOICE: 0, SMS: 0, DATA: 0 };
                acc[date][cdr.service] += cdr.usageAmount;
                return acc;
            }, {});
            return `
                <h3>Daily Usage per Service</h3>
                <table border="1">
                    <tr><th>Date</th><th>VOICE</th><th>SMS</th><th>DATA</th></tr>
                    ${Object.entries(dailyService).map(([date, usage]) =>
                `<tr><td>${date}</td><td>${usage.VOICE}</td><td>${usage.SMS}</td><td>${usage.DATA}</td></tr>`).join('')}
                </table>
            `;
        }
        return '<p>Invalid report type.</p>';
    }

    if (searchForm) {
        searchForm.addEventListener('submit', (e) => {
            e.preventDefault();
            const query = document.getElementById('searchQuery').value;
            const field = document.getElementById('searchField').value;
            const filtered = searchCdrs(cdrs, query, field);
            renderCdrTable(filtered);
        });
    }

    if (clearSearchBtn) {
        clearSearchBtn.addEventListener('click', () => {
            document.getElementById('searchQuery').value = '';
            renderCdrTable(cdrs);
        });
    }

    if (sortForm) {
        sortForm.addEventListener('submit', (e) => {
            e.preventDefault();
            const field = document.getElementById('sortField').value;
            const direction = document.getElementById('sortDirection').value;
            const query = document.getElementById('searchQuery').value;
            const filtered = searchCdrs(cdrs, query);
            const sorted = sortCdrs(filtered, field, direction);

            renderCdrTable(sorted);
        });
    }

    if (reportForm) {
        reportForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            usageReportDiv.innerHTML = 'Loading...';
            try {
                const validToken = await getValidToken();
                if (!validToken) return;

                const response = await fetch('http://localhost:8084/cdrs', {
                    method: 'GET',
                    headers: {
                        'Authorization': `Bearer ${validToken}`,
                        'Content-Type': 'application/json'
                    }
                });

                if (!response.ok) throw new Error(`HTTP ${response.status}`);
                const data = await response.json();

                const reportType = document.getElementById('reportType').value;
                const fromDate = document.getElementById('fromDate').value;

                const filtered = filterCdrsByDate(data, fromDate);
                console.log('Fetched CDRs:', data);
                console.log('Filtered CDRs:', filtered);
                console.log('Report Type:', reportType);

                const html = generateReport(reportType, filtered);
                console.log('Generated Report HTML:', html);

                usageReportDiv.innerHTML = html;
            } catch (err) {
                usageReportDiv.innerHTML = `Error: ${err.message}`;
            }
        });
    }
});
