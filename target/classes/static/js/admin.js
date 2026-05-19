/**
 * Admin Dashboard – Real-time JavaScript
 * Handles: WebSocket connection, order table updates,
 *          countdown timers, status modal, toast notifications.
 */

// ─────────────────────────────────────────────
// WebSocket Setup (STOMP over SockJS)
// ─────────────────────────────────────────────
let stompClient = null;
let notifCount = 0;
let currentOrderId = null;

function connectWebSocket() {
  const socket = new SockJS('/ws-orders');
  stompClient = Stomp.over(socket);
  stompClient.debug = null; // suppress verbose logs

  stompClient.connect({}, () => {
    setWsStatus(true);

    // Subscribe to order updates → refresh table row
    stompClient.subscribe('/topic/orders', (msg) => {
      const data = JSON.parse(msg.body);
      handleOrderUpdate(data);
    });

    // Subscribe to notifications → bell + panel
    stompClient.subscribe('/topic/notifications', (msg) => {
      const data = JSON.parse(msg.body);
      addToActivityFeed(data);
      showToast(getToastIcon(data.type), data.message, getToastClass(data.type));
      incrementNotifBadge();
    });
  }, () => {
    setWsStatus(false);
    // Attempt reconnect after 5 seconds
    setTimeout(connectWebSocket, 5000);
  });
}

function setWsStatus(connected) {
  const dot = document.querySelector('.ws-dot');
  const label = document.getElementById('wsStatus');
  if (connected) {
    dot.classList.add('connected');
    dot.classList.remove('disconnected');
    label.textContent = 'Live';
  } else {
    dot.classList.remove('connected');
    dot.classList.add('disconnected');
    label.textContent = 'Reconnecting...';
  }
}

// ─────────────────────────────────────────────
// Handle incoming order updates
// ─────────────────────────────────────────────
function handleOrderUpdate(data) {
  const order = data.order;
  if (!order) return;

  const existingRow = document.getElementById('row-' + order.id);
  if (existingRow) {
    updateTableRow(existingRow, order);
  } else if (data.type === 'ORDER_PLACED') {
    prependTableRow(order);
  }

  // Remove empty-state row if present
  const emptyRow = document.querySelector('#ordersBody .empty-state');
  if (emptyRow) emptyRow.closest('tr').remove();
}

function updateTableRow(row, order) {
  // Update status badge
  const statusBadge = row.querySelector('.status-badge');
  if (statusBadge) {
    statusBadge.className = 'status-badge status-' + order.orderStatus.toLowerCase();
    statusBadge.textContent = order.orderStatus;
    statusBadge.setAttribute('data-order-id', order.id);
  }

  // Update payment badge
  const cells = row.querySelectorAll('td');
  if (cells[6]) {
    cells[6].innerHTML = order.paymentStatus === 'COMPLETED'
      ? '<span class="badge-paid">COMPLETED</span>'
      : '<span class="badge-pending">PENDING</span>';
  }

  // Update countdown cell attributes
  const countdownCell = row.querySelector('.countdown-cell');
  if (countdownCell) {
    countdownCell.setAttribute('data-placed', order.placedAt || '');
    countdownCell.setAttribute('data-shipped', order.shippedAt || '');
    countdownCell.setAttribute('data-status', order.orderStatus);
  }

  // Flash highlight animation
  row.style.background = 'rgba(99,102,241,0.15)';
  setTimeout(() => { row.style.background = ''; }, 1500);
}

function prependTableRow(order) {
  const tbody = document.getElementById('ordersBody');
  const tr = document.createElement('tr');
  tr.id = 'row-' + order.id;
  tr.className = 'order-row';
  tr.style.animation = 'slideIn 0.4s ease';
  tr.innerHTML = `
    <td class="order-id">#${order.id}</td>
    <td>${order.userId}</td>
    <td><span class="stock-symbol">${escapeHtml(order.productName)}</span></td>
    <td><span class="badge-buy">${escapeHtml(order.category)}</span></td>
    <td>${order.quantity}</td>
    <td class="price-cell">$${parseFloat(order.price).toFixed(2)}</td>
    <td><span class="badge-pending">PENDING</span></td>
    <td><span class="status-badge status-placed" data-order-id="${order.id}">PLACED</span></td>
    <td class="countdown-cell" data-placed="${order.placedAt || ''}" data-shipped="" data-status="PLACED">
      <span class="countdown-timer">—</span>
    </td>
    <td class="time-cell">${formatTime(new Date())}</td>
    <td>
      <button class="btn-action" onclick="openStatusModal(${order.id}, 'PLACED')">
        <i class="fas fa-edit"></i> Update
      </button>
    </td>
  `;
  tbody.prepend(tr);
}

// ─────────────────────────────────────────────
// Countdown Timers
// ─────────────────────────────────────────────
function updateCountdowns() {
  document.querySelectorAll('.countdown-cell').forEach(cell => {
    const status = cell.getAttribute('data-status');
    const placedStr = cell.getAttribute('data-placed');
    const shippedStr = cell.getAttribute('data-shipped');
    const timerEl = cell.querySelector('.countdown-timer');
    if (!timerEl) return;

    if (status === 'PLACED' || status === 'PAID') {
      if (!placedStr) { timerEl.textContent = '—'; return; }
      const placed = new Date(placedStr.replace('T',' '));
      const deadline = new Date(placed.getTime() + 24 * 60 * 60 * 1000);
      renderCountdown(timerEl, deadline, 'Ship by');

    } else if (status === 'SHIPPED') {
      if (!shippedStr) { timerEl.textContent = '—'; return; }
      const shipped = new Date(shippedStr.replace('T',' '));
      const deadline = new Date(shipped.getTime() + 48 * 60 * 60 * 1000);
      renderCountdown(timerEl, deadline, 'Deliver by');

    } else {
      timerEl.textContent = '—';
      timerEl.className = 'countdown-timer';
    }
  });
}

function renderCountdown(el, deadline, label) {
  const now = new Date();
  const diff = deadline - now;

  if (diff <= 0) {
    el.textContent = '⚠ OVERDUE';
    el.className = 'countdown-timer urgent';
    return;
  }

  const hours = Math.floor(diff / 3600000);
  const minutes = Math.floor((diff % 3600000) / 60000);
  const seconds = Math.floor((diff % 60000) / 1000);

  el.textContent = `${pad(hours)}:${pad(minutes)}:${pad(seconds)}`;
  el.className = 'countdown-timer ' + (hours < 4 ? 'urgent' : 'safe');
}

function pad(n) { return String(n).padStart(2, '0'); }

// ─────────────────────────────────────────────
// Status Update Modal
// ─────────────────────────────────────────────
function openStatusModal(orderId, currentStatus) {
  currentOrderId = orderId;
  document.getElementById('modalOrderId').textContent = '#' + orderId;
  const currentEl = document.getElementById('modalCurrentStatus');
  currentEl.className = 'status-badge status-' + currentStatus.toLowerCase();
  currentEl.textContent = currentStatus;
  document.getElementById('statusModal').classList.add('open');
}

function closeStatusModal() {
  document.getElementById('statusModal').classList.remove('open');
  currentOrderId = null;
}

function updateStatus(newStatus) {
  if (!currentOrderId) return;

  fetch(`/admin/orders/${currentOrderId}/status`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ status: newStatus })
  })
  .then(async res => {
    if (res.ok) {
      showToast('✅', `Order #${currentOrderId} → ${newStatus}`, 'success');
      closeStatusModal();
    } else {
      const err = await res.text();
      showToast('❌', 'Error: ' + err, 'error');
    }
  })
  .catch(() => showToast('❌', 'Network error', 'error'));
}

// Click outside modal to close
document.getElementById('statusModal').addEventListener('click', function(e) {
  if (e.target === this) closeStatusModal();
});

// ─────────────────────────────────────────────
// Activity Feed
// ─────────────────────────────────────────────
function addToActivityFeed(data) {
  const feed = document.getElementById('activityFeed');
  const emptyEl = feed.querySelector('.activity-empty');
  if (emptyEl) emptyEl.remove();

  const item = document.createElement('div');
  item.className = 'activity-item';
  item.style.animation = 'slideIn 0.3s ease';
  item.innerHTML = `
    <div class="activity-dot ${data.type || ''}"></div>
    <div class="activity-content">
      <span class="activity-msg">${escapeHtml(data.message)}</span>
      <span class="activity-time">${formatTime(new Date())}</span>
    </div>
  `;
  feed.prepend(item);

  // Keep max 50 items
  const items = feed.querySelectorAll('.activity-item');
  if (items.length > 50) items[items.length - 1].remove();
}

// ─────────────────────────────────────────────
// Toast Notifications
// ─────────────────────────────────────────────
function showToast(icon, message, type = 'info') {
  const container = document.getElementById('toastContainer');
  const toast = document.createElement('div');
  toast.className = `toast ${type}`;
  toast.innerHTML = `<span class="toast-icon">${icon}</span><span class="toast-msg">${escapeHtml(message)}</span>`;
  container.appendChild(toast);
  setTimeout(() => toast.remove(), 4000);
}

function getToastIcon(type) {
  const icons = {
    ORDER_PLACED: '📋', PAYMENT_COMPLETED: '💳', ORDER_SHIPPED: '🚀',
    ORDER_DELIVERED: '✅', ORDER_CANCELLED: '❌', REMINDER: '⚠️'
  };
  return icons[type] || '🔔';
}

function getToastClass(type) {
  const classes = {
    ORDER_PLACED: 'info', PAYMENT_COMPLETED: 'success', ORDER_SHIPPED: 'warning',
    ORDER_DELIVERED: 'success', ORDER_CANCELLED: 'error', REMINDER: 'warning'
  };
  return classes[type] || 'info';
}

// ─────────────────────────────────────────────
// Notification Bell
// ─────────────────────────────────────────────
function incrementNotifBadge() {
  notifCount++;
  const badge = document.getElementById('notifCount');
  badge.textContent = notifCount;
  badge.style.display = 'flex';
}

document.getElementById('notifBell').addEventListener('click', () => {
  const panel = document.getElementById('notifPanel');
  panel.style.display = panel.style.display === 'none' ? 'flex' : 'none';
  notifCount = 0;
  document.getElementById('notifCount').style.display = 'none';
});

function closeNotifPanel() {
  document.getElementById('notifPanel').style.display = 'none';
}

// ─────────────────────────────────────────────
// Order Table Search
// ─────────────────────────────────────────────
document.getElementById('orderSearch').addEventListener('input', function() {
  const term = this.value.toLowerCase();
  document.querySelectorAll('#ordersBody .order-row').forEach(row => {
    row.style.display = row.textContent.toLowerCase().includes(term) ? '' : 'none';
  });
});

// ─────────────────────────────────────────────
// Utilities
// ─────────────────────────────────────────────
function formatTime(date) {
  return date.toLocaleTimeString('en-GB', { hour: '2-digit', minute: '2-digit', second: '2-digit' });
}

function escapeHtml(str) {
  const div = document.createElement('div');
  div.appendChild(document.createTextNode(str || ''));
  return div.innerHTML;
}

// ─────────────────────────────────────────────
// Init
// ─────────────────────────────────────────────
document.addEventListener('DOMContentLoaded', () => {
  connectWebSocket();
  updateCountdowns();
  setInterval(updateCountdowns, 1000);
});
