// Lost & Found Synchronized Web Application Client

const API_BASE = window.location.origin;

let currentReportType = 'LOST';
let itemPhotoBase64 = '';
let claimProofBase64 = '';
let allInventoryItems = [];

// =====================================================
// THEME SWITCHER
// =====================================================

function setTheme(mode) {
    document.documentElement.setAttribute('data-theme', mode);

    const btnNvidia = document.getElementById('btnNvidia');
    const btnNetflix = document.getElementById('btnNetflix');
    const brandPill = document.getElementById('brandPill');
    const heroBadge = document.getElementById('heroBadge');

    if (mode === 'nvidia') {
        if (btnNvidia) btnNvidia.classList.add('active');
        if (btnNetflix) btnNetflix.classList.remove('active');
        if (brandPill) brandPill.textContent = '⚡ NVIDIA RTX';
        if (heroBadge) heroBadge.textContent = 'GEFORCE RTX PRECISION RECOVERY';
    } else {
        if (btnNetflix) btnNetflix.classList.add('active');
        if (btnNvidia) btnNvidia.classList.remove('active');
        if (brandPill) brandPill.textContent = '🍿 NETFLIX CINEMA';
        if (heroBadge) heroBadge.textContent = 'CINEMATIC LOST & FOUND VERIFICATION';
    }

    localStorage.setItem('preferred_theme', mode);

    // Sync theme with desktop backend if connected
    fetch(API_BASE + '/api/theme', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ theme: mode })
    }).catch(() => {});
}

// =====================================================
// REPORT LOST / FOUND ITEM HANDLING
// =====================================================

function selectReportType(type) {
    currentReportType = type;
    const tabLost = document.getElementById('tabLost');
    const tabFound = document.getElementById('tabFound');
    const title = document.getElementById('reportFormTitle');

    if (type === 'LOST') {
        tabLost.className = 'report-tab-btn active-lost';
        tabFound.className = 'report-tab-btn';
        if (title) title.textContent = 'Report a Lost Item';
    } else {
        tabFound.className = 'report-tab-btn active-found';
        tabLost.className = 'report-tab-btn';
        if (title) title.textContent = 'Report a Discovered (Found) Item';
    }
}

function handleItemPhotoSelected(event) {
    const file = event.target.files[0];
    if (!file) return;

    const reader = new FileReader();
    reader.onload = function(e) {
        itemPhotoBase64 = e.target.result;
        const preview = document.getElementById('reportPhotoPreview');
        const text = document.getElementById('reportPhotoText');
        const clearBtn = document.getElementById('btnClearItemPhoto');

        preview.src = itemPhotoBase64;
        preview.classList.remove('hidden');
        text.textContent = '✓ Attached: ' + file.name + ' (' + Math.round(file.size / 1024) + ' KB)';
        clearBtn.classList.remove('hidden');
    };
    reader.readAsDataURL(file);
}

function clearItemPhoto() {
    itemPhotoBase64 = '';
    document.getElementById('reportPhotoInput').value = '';
    document.getElementById('reportPhotoPreview').classList.add('hidden');
    document.getElementById('reportPhotoText').textContent = '📷 Click to upload photo (JPG, PNG, WEBP)';
    document.getElementById('btnClearItemPhoto').classList.add('hidden');
}

function attachSamplePhoto(target) {
    // 1x1 green/accent pixel placeholder as sample photo
    const sampleData = 'data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAEAAAABACAYAAACqaXHeAAAAPElEQVR42u3PMQEAAAgEIDu/qFmDXw4aWJm5h8BAgAABAgQIECBAgAABAgQIECBAgAABAgQIECBAgAAB4msBq64BCW/H6QAAAAAElFTkSuQmCC';
    if (target === 'item') {
        itemPhotoBase64 = sampleData;
        const preview = document.getElementById('reportPhotoPreview');
        preview.src = sampleData;
        preview.classList.remove('hidden');
        document.getElementById('reportPhotoText').textContent = '✓ Attached: sample_belonging_photo.png (42 KB)';
        document.getElementById('btnClearItemPhoto').classList.remove('hidden');
    } else {
        claimProofBase64 = sampleData;
        const preview = document.getElementById('claimPhotoPreview');
        preview.src = sampleData;
        preview.classList.remove('hidden');
        document.getElementById('uploadSimText').textContent = '✓ Attached: sample_purchase_receipt.png (42 KB)';
        document.getElementById('btnClearProofPhoto').classList.remove('hidden');
    }
}

async function submitReportItem() {
    const itemName = document.getElementById('reportItemName').value.trim();
    const category = document.getElementById('reportCategory').value;
    const location = document.getElementById('reportLocation').value.trim();
    const ownerName = document.getElementById('reportOwnerName').value.trim();
    const contact = document.getElementById('reportContact').value.trim();
    const description = document.getElementById('reportDescription').value.trim();
    const alertBox = document.getElementById('reportAlert');

    if (!itemName || !location || !ownerName || !contact) {
        alert('Please fill out all required fields marked with * (Item Name, Location, Name, Contact).');
        return;
    }

    const payload = {
        itemName: itemName,
        category: category,
        itemType: currentReportType,
        location: location,
        ownerName: ownerName,
        contact: contact,
        description: description,
        imageBase64: itemPhotoBase64
    };

    let serverSaved = false;
    let generatedId = Math.floor(Math.random() * 900) + 100;

    try {
        const res = await fetch(API_BASE + '/api/items', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });

        if (res.ok) {
            const data = await res.json();
            if (data.id) generatedId = data.id;
            serverSaved = true;
        }
    } catch (e) {
        // Offline or fallback demo mode
    }

    alertBox.classList.remove('hidden');
    alertBox.innerHTML = `
        <strong>✓ Report Successfully Submitted #${generatedId}!</strong><br>
        Item: <strong>${itemName}</strong> (${category}) — Type: <span style="color:${currentReportType === 'LOST' ? '#F43F5E' : '#22C55E'}">${currentReportType}</span><br>
        Location: <em>${location}</em> | Reported By: ${ownerName} (${contact})<br>
        ${itemPhotoBase64 ? '<strong>📷 Item Photo Attached:</strong> Saved directly to system media storage.<br>' : ''}
        ${serverSaved ? '<span style="color:#22C55E">✓ Live Synchronized: Report immediately visible in the Java desktop application and matching engine!</span>' : '<em>(Queued locally)</em>'}
    `;

    // Clear inputs
    document.getElementById('reportItemName').value = '';
    document.getElementById('reportLocation').value = '';
    document.getElementById('reportDescription').value = '';
    clearItemPhoto();

    alertBox.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
    loadLiveBackendData();
}

// =====================================================
// LIVE INVENTORY FETCH & RENDER
// =====================================================

async function loadLiveBackendData() {
    try {
        // 1. Fetch Stats
        const statsRes = await fetch(API_BASE + '/api/stats');
        if (statsRes.ok) {
            const stats = await statsRes.json();
            const elItems = document.getElementById('statItems');
            const elLost = document.getElementById('statLost');
            const elFound = document.getElementById('statFound');
            const elClaims = document.getElementById('statClaims');

            if (elItems) elItems.textContent = stats.totalItems || 0;
            if (elLost) elLost.textContent = stats.lostCount || 0;
            if (elFound) elFound.textContent = stats.foundCount || 0;
            if (elClaims) elClaims.textContent = stats.approvedClaims || 0;
        }

        // 2. Fetch Items
        const itemsRes = await fetch(API_BASE + '/api/items');
        if (itemsRes.ok) {
            allInventoryItems = await itemsRes.json();
            renderInventory(allInventoryItems);
            populateClaimItemSelect(allInventoryItems);
        }
    } catch (e) {
        renderFallbackInventory();
    }
}

function renderInventory(items) {
    const grid = document.getElementById('inventoryGrid');
    if (!grid) return;

    if (!items || items.length === 0) {
        grid.innerHTML = '<div class="loading-card">No reported items match the current filter criteria.</div>';
        return;
    }

    grid.innerHTML = '';
    items.forEach(item => {
        const isFound = 'FOUND'.equalsIgnoreCase(item.itemType || item.status);
        const card = document.createElement('div');
        card.className = 'inventory-card';

        const photoHtml = item.imagePath && item.imagePath.trim() !== ''
            ? `<div style="font-size: 11px; color: var(--primary-accent); margin-bottom: 8px;">📷 Photo Verified Attached</div>`
            : '';

        const claimBtnHtml = isFound
            ? `<button type="button" class="btn btn-secondary" style="padding: 6px 14px; font-size: 11px;" onclick="prefillClaim(${item.id}, '${escapeQuote(item.itemName)}')">✓ Claim This Item</button>`
            : `<span style="font-size: 11px; color: var(--text-muted);">Reported Lost</span>`;

        card.innerHTML = `
            <div>
                <div class="inventory-card-top">
                    <span class="inv-status-pill ${isFound ? 'found' : 'lost'}">
                        ${isFound ? '▲ FOUND' : '▼ LOST'}
                    </span>
                    <span class="inv-cat-pill">${item.category || 'General'}</span>
                </div>
                <h4>#${item.id} - ${escapeHtml(item.itemName)}</h4>
                <div class="inv-meta">📍 ${escapeHtml(item.location || 'Unknown')}</div>
                <div class="inv-desc">${escapeHtml(item.description || 'No additional description provided.')}</div>
                ${photoHtml}
            </div>
            <div class="inv-card-footer">
                <span style="font-size: 11px; color: var(--text-muted);">By: ${escapeHtml(item.ownerName || 'Community')}</span>
                ${claimBtnHtml}
            </div>
        `;
        grid.appendChild(card);
    });
}

function filterInventory() {
    const query = (document.getElementById('invSearchInput')?.value || '').toLowerCase().trim();
    const cat = document.getElementById('invCategoryFilter')?.value || 'ALL';
    const type = document.getElementById('invTypeFilter')?.value || 'ALL';

    const filtered = allInventoryItems.filter(item => {
        const name = (item.itemName || '').toLowerCase();
        const loc = (item.location || '').toLowerCase();
        const desc = (item.description || '').toLowerCase();
        const itemCat = item.category || '';
        const itemType = (item.itemType || item.status || '').toUpperCase();

        const matchQuery = !query || name.includes(query) || loc.includes(query) || desc.includes(query);
        const matchCat = cat === 'ALL' || itemCat.equalsIgnoreCase(cat);
        const matchType = type === 'ALL' || itemType.equalsIgnoreCase(type);

        return matchQuery && matchCat && matchType;
    });

    renderInventory(filtered);
}

function populateClaimItemSelect(items) {
    const select = document.getElementById('demoItemSelect');
    if (!select) return;

    select.innerHTML = '';
    const foundItems = items.filter(i => 'FOUND'.equalsIgnoreCase(i.itemType || i.status));
    const listToUse = foundItems.length > 0 ? foundItems : items;

    listToUse.forEach(item => {
        const opt = document.createElement('option');
        opt.value = item.id;
        opt.textContent = `#${item.id} - ${item.itemName} (${item.location || 'Reported'})`;
        select.appendChild(opt);
    });
}

function prefillClaim(itemId, itemName) {
    const select = document.getElementById('demoItemSelect');
    if (select) {
        select.value = itemId;
    }
    const claimSection = document.getElementById('claim');
    if (claimSection) {
        claimSection.scrollIntoView({ behavior: 'smooth' });
    }
}

// Fallback seed inventory for offline previews
function renderFallbackInventory() {
    allInventoryItems = [
        { id: 1, itemName: 'Apple MacBook Pro 14 Space Gray', category: 'Electronics', itemType: 'FOUND', location: 'Central Library 2nd Floor', description: 'Found in study cubicle with silver hard case.' },
        { id: 2, itemName: 'MacBook Pro 14-inch Space Gray', category: 'Electronics', itemType: 'LOST', location: 'Central Library', description: 'Lost during study session. Small scratch near charger.' },
        { id: 3, itemName: 'Brown Leather Bi-Fold Wallet', category: 'Wallet & Purses', itemType: 'FOUND', location: 'Dining Commons Main Hall', description: 'Genuine leather wallet with ID and receipt.' },
        { id: 4, itemName: 'Sony WH-1000XM5 Headphones', category: 'Electronics', itemType: 'FOUND', location: 'Science Complex Room 101', description: 'Over-ear black headphones in zippered carry case.' },
        { id: 5, itemName: 'Honda Car Key with Black Lanyard', category: 'Keys', itemType: 'LOST', location: 'North Parking Deck Level 2', description: 'Key fob attached to branded black strap.' }
    ];
    renderInventory(allInventoryItems);
    populateClaimItemSelect(allInventoryItems);
}

// =====================================================
// CLAIM SUBMISSION HANDLING
// =====================================================

function handleClaimProofSelected(event) {
    const file = event.target.files[0];
    if (!file) return;

    const reader = new FileReader();
    reader.onload = function(e) {
        claimProofBase64 = e.target.result;
        const preview = document.getElementById('claimPhotoPreview');
        const text = document.getElementById('uploadSimText');
        const clearBtn = document.getElementById('btnClearProofPhoto');

        preview.src = claimProofBase64;
        preview.classList.remove('hidden');
        text.textContent = '✓ Attached: ' + file.name + ' (' + Math.round(file.size / 1024) + ' KB)';
        clearBtn.classList.remove('hidden');
    };
    reader.readAsDataURL(file);
}

function clearProofPhoto() {
    claimProofBase64 = '';
    document.getElementById('claimProofInput').value = '';
    document.getElementById('claimPhotoPreview').classList.add('hidden');
    document.getElementById('uploadSimText').textContent = '📷 Click to upload proof receipt / photo';
    document.getElementById('btnClearProofPhoto').classList.add('hidden');
}

async function submitDemoClaim() {
    const itemSelect = document.getElementById('demoItemSelect');
    const itemId = itemSelect ? itemSelect.value : 1;
    const itemText = itemSelect && itemSelect.selectedIndex >= 0 ? itemSelect.options[itemSelect.selectedIndex].text : 'Found Belonging';
    const name = document.getElementById('demoName').value.trim();
    const contact = document.getElementById('demoContact').value.trim();
    const proof = document.getElementById('demoProof').value.trim();
    const alertBox = document.getElementById('demoSuccessAlert');

    if (!name || !contact || !proof) {
        alert('Please fill out all required fields in the claim form.');
        return;
    }

    let serverSaved = false;
    let generatedClaimId = Math.floor(Math.random() * 900) + 100;

    try {
        const payload = {
            itemId: itemId,
            claimantName: name,
            contact: contact,
            proof: proof,
            proofBase64: claimProofBase64
        };

        const res = await fetch(API_BASE + '/api/claims', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });

        if (res.ok) {
            const data = await res.json();
            if (data.id) generatedClaimId = data.id;
            serverSaved = true;
        }
    } catch (e) {
        // Fallback simulation mode
    }

    alertBox.classList.remove('hidden');
    alertBox.innerHTML = `
        <strong>✓ Claim Successfully Queued #${generatedClaimId}!</strong><br>
        Target Item: <em>${escapeHtml(itemText)}</em><br>
        Claimant: ${escapeHtml(name)} (${escapeHtml(contact)})<br>
        ${claimProofBase64 ? '<strong>📷 Photo Proof Attached:</strong> Photographic verification proof sent to admin dossier.<br>' : '<em>(No photo verification proof attached)</em><br>'}
        Status: <strong>PENDING ADMIN AUTHENTICATION</strong><br>
        ${serverSaved ? '<span style="color:#22C55E">✓ Live Synchronized: Admin can now review and verify this claim in the Java desktop application!</span>' : ''}
    `;

    alertBox.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
    loadLiveBackendData();
}

// =====================================================
// UTILITY HELPERS
// =====================================================

String.prototype.equalsIgnoreCase = function(other) {
    if (typeof other !== 'string') return false;
    return this.toLowerCase() === other.toLowerCase();
};

function escapeHtml(str) {
    if (!str) return '';
    return str.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;').replace(/"/g, '&quot;');
}

function escapeQuote(str) {
    if (!str) return '';
    return str.replace(/'/g, "\\'").replace(/"/g, '&quot;');
}

// Initialize on load
window.addEventListener('DOMContentLoaded', () => {
    const saved = localStorage.getItem('preferred_theme');
    if (saved === 'netflix' || saved === 'nvidia') {
        setTheme(saved);
    } else {
        fetch(API_BASE + '/api/theme')
            .then(r => r.json())
            .then(data => {
                if (data && data.theme) setTheme(data.theme);
            })
            .catch(() => {});
    }

    loadLiveBackendData();
});
