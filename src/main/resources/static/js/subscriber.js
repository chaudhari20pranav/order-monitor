/**
 * Subscriber Dashboard – Real-time JavaScript
 * Handles: WebSocket connection, place/pay/cancel orders,
 *          live order card updates, countdown timers, toasts.
 */

// ─────────────────────────────────────────────
// State
// ─────────────────────────────────────────────
let stompClient = null;
const currentUserId = parseInt(document.getElementById('currentUserId')?.value || '0', 10);

// ─────────────────────────────────────────────
// WebSocket Setup
// ─────────────────────────────────────────────
function connectWebSocket() {
  const socket = new SockJS('/ws-orders');
  stompClient = Stomp.over(socket);
  stompClient.debug = null;

  stompClient.connect({}, () => {
    setWsStatus(true);

    // Listen to order updates – only show ones for this user
    stompClient.subscribe('/topic/orders', (msg) => {
      const data = JSON.parse(msg.body);
      if (!data.order) return;

      // Only process events belonging to this subscriber
      if (data.targetUserId && data.targetUserId !== currentUserId) return;

      handleIncomingUpdate(data);
    });

    // Listen to general notifications
    stompClient.subscribe('/topic/notifications', (msg) => {
      const data = JSON.parse(msg.body);
      if (data.targetUserId && data.targetUserId !== currentUserId) return;
      showToast(getToastIcon(data.type), data.message, getToastClass(data.type));
      addNotifToDash(data.message);
    });

  }, () => {
    setWsStatus(false);
    setTimeout(connectWebSocket, 5000);
  });
}

function setWsStatus(connected) {
  const dot = document.querySelector('.ws-dot');
  const label = document.getElementById('wsStatus');
  dot.classList.toggle('connected', connected);
  dot.classList.toggle('disconnected', !connected);
  label.textContent = connected ? 'Live' : 'Reconnecting...';
}

// ─────────────────────────────────────────────
// Handle incoming WS events
// ─────────────────────────────────────────────
function handleIncomingUpdate(data) {
  const order = data.order;
  const type = data.type;

  if (type === 'ORDER_PLACED') {
    prependLiveOrderCard(order);
    updateStatCounts();
  } else if (type === 'PAYMENT_COMPLETED') {
    updateOrderCardStatus(order);
  } else if (type === 'ORDER_SHIPPED') {
    updateOrderCardStatus(order);
  } else if (type === 'ORDER_DELIVERED') {
    moveOrderToCompleted(order);
    updateStatCounts();
  } else if (type === 'ORDER_CANCELLED') {
    moveOrderToCancelled(order);
    updateStatCounts();
  }
}

// ─────────────────────────────────────────────
// Place Order
// ─────────────────────────────────────────────
function placeOrder() {
  const productName = document.getElementById('productName').value.trim();
  const category  = document.getElementById('category').value;
  const quantity = parseInt(document.getElementById('quantity').value, 10);
  const price    = parseFloat(document.getElementById('price').value);

  if (!productName) { showToast('⚠️', 'Product name is required', 'error'); return; }
  if (productName.length > 100) { showToast('⚠️', 'Product name too long', 'error'); return; }
  if (!quantity || quantity < 1){ showToast('⚠️', 'Quantity must be at least 1', 'error'); return; }
  if (!price || price <= 0)     { showToast('⚠️', 'Price must be positive', 'error'); return; }

  const btn = document.getElementById('placeOrderBtn');
  btn.disabled = true;
  btn.innerHTML = '<i class="fas fa-spinner fa-spin me-2"></i>Placing...';

  fetch('/subscriber/orders', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ productName, category, quantity, price })
  })
  .then(async res => {
    if (res.ok) {
      showToast('🛒', 'Order placed successfully!', 'success');
      clearOrderForm();
    } else {
      const err = await res.text();
      showToast('❌', parseError(err), 'error');
    }
  })
  .catch(() => showToast('❌', 'Network error – please retry', 'error'))
  .finally(() => {
    btn.disabled = false;
    btn.innerHTML = '<i class="fas fa-shopping-cart me-2"></i>Place Order';
  });
}

function clearOrderForm() {
  document.getElementById('productName').value = '';
  document.getElementById('quantity').value = '';
  document.getElementById('price').value = '';
  document.getElementById('category').value = 'Electronics';
  
}

// ─────────────────────────────────────────────
// Pay Order
// ─────────────────────────────────────────────
function payOrder(orderId) {
  if (!confirm('Confirm payment for Order #' + orderId + '?')) return;

  fetch(`/subscriber/orders/${orderId}/pay`, { method: 'POST' })
  .then(async res => {
    if (res.ok) {
      showToast('💳', `Payment confirmed for Order #${orderId}`, 'success');
    } else {
      const err = await res.text();
      showToast('❌', parseError(err), 'error');
    }
  })
  .catch(() => showToast('❌', 'Network error', 'error'));
}

// ─────────────────────────────────────────────
// Cancel Order
// ─────────────────────────────────────────────
function cancelOrder(orderId) {
  if (!confirm('Cancel Order #' + orderId + '? This cannot be undone.')) return;

  fetch(`/subscriber/orders/${orderId}/cancel`, { method: 'POST' })
  .then(async res => {
    if (res.ok) {
      showToast('❌', `Order #${orderId} cancelled`, 'warning');
    } else {
      const err = await res.text();
      showToast('❌', parseError(err), 'error');
    }
  })
  .catch(() => showToast('❌', 'Network error', 'error'));
}

// ─────────────────────────────────────────────
// Mark All Notifications Read
// ─────────────────────────────────────────────
function markAllRead() {
  fetch('/subscriber/notifications/read', { method: 'POST' })
  .then(res => {
    if (res.ok) {
      document.querySelectorAll('.notif-item-dash.unread').forEach(el => {
        el.classList.replace('unread', 'read');
      });
      const badge = document.querySelector('.nav-badge');
      if (badge) badge.remove();
      showToast('✅', 'All notifications marked as read', 'success');
    }
  });
}

// ─────────────────────────────────────────────
// DOM Updates from WebSocket
// ─────────────────────────────────────────────
function prependLiveOrderCard(order) {
  const container = document.getElementById('liveOrdersContainer');
  const empty = container.querySelector('.empty-state');
  if (empty) empty.remove();

  const card = buildOrderCard(order);
  container.prepend(card);
}

function buildOrderCard(order) {
  const div = document.createElement('div');
  div.className = 'order-card';
  div.id = 'order-' + order.id;
  div.setAttribute('data-status', order.orderStatus);
  div.setAttribute('data-placed', order.placedAt || '');
  div.setAttribute('data-shipped', order.shippedAt || '');
  div.style.animation = 'slideIn 0.4s ease';

  const payClass  = order.paymentStatus === 'COMPLETED' ? 'badge-paid' : 'badge-pending';
  const statusCls = 'status-' + order.orderStatus.toLowerCase();

  const showPay    = order.paymentStatus === 'PENDING';
  const showCancel = order.orderStatus !== 'CANCELLED' && order.orderStatus !== 'DELIVERED';

  div.innerHTML = `
    <div class="order-card-header">
      <div class="order-card-id">#${order.id}</div>
      <span class="status-badge ${statusCls}">${order.orderStatus}</span>
    </div>
    <div class="order-card-body">
      <div class="order-detail">
        <span class="detail-label">Product</span>
        <span class="stock-symbol">${escapeHtml(order.productName)}</span>
      </div>
      <div class="order-detail">
        <span class="detail-label">Category</span>
        <span class="badge-buy">${escapeHtml(order.category)}</span>
      </div>
      <div class="order-detail">
        <span class="detail-label">Qty</span>
        <span>${order.quantity}</span>
      </div>
      <div class="order-detail">
        <span class="detail-label">Unit Price</span>
        <span class="price-cell">$${parseFloat(order.price).toFixed(2)}</span>
      </div>
      <div class="order-detail">
        <span class="detail-label">Payment</span>
        <span class="${payClass}">${order.paymentStatus}</span>
      </div>
      <div class="order-detail">
        <span class="detail-label">Countdown</span>
        <span class="countdown-timer" data-order-id="${order.id}">—</span>
      </div>
    </div>
    <div class="order-card-actions">
      ${showPay    ? `<button class="btn-pay" onclick="payOrder(${order.id})"><i class="fas fa-credit-card me-1"></i> Pay Now</button>` : ''}
      ${showCancel ? `<button class="btn-cancel-order" onclick="cancelOrder(${order.id})"><i class="fas fa-times me-1"></i> Cancel</button>` : ''}
    </div>
  `;
  return div;
}

function updateOrderCardStatus(order) {
  const card = document.getElementById('order-' + order.id);
  if (!card) return;

  // Update status badge
  const badge = card.querySelector('.status-badge');
  if (badge) {
    badge.className = 'status-badge status-' + order.orderStatus.toLowerCase();
    badge.textContent = order.orderStatus;
  }

  // Update payment badge
  const payBadge = card.querySelectorAll('.order-detail')[4]?.querySelector('span:last-child');
  if (payBadge && order.paymentStatus === 'COMPLETED') {
    payBadge.className = 'badge-paid';
    payBadge.textContent = 'COMPLETED';
    // Remove Pay Now button
    const payBtn = card.querySelector('.btn-pay');
    if (payBtn) payBtn.remove();
  }

  // Update data attributes for countdown
  card.setAttribute('data-status', order.orderStatus);
  if (order.shippedAt) card.setAttribute('data-shipped', order.shippedAt);

  // Flash
  card.style.outline = '2px solid rgba(99,102,241,0.5)';
  setTimeout(() => { card.style.outline = ''; }, 1500);
}

function moveOrderToCompleted(order) {
  const card = document.getElementById('order-' + order.id);
  if (card) card.remove();

  // Reload page to properly show in completed section
  // (alternatively you could build the completed card here)
  showToast('✅', `Order #${order.id} delivered! Page refreshing...`, 'success');
  setTimeout(() => location.reload(), 2000);
}

function moveOrderToCancelled(order) {
  const card = document.getElementById('order-' + order.id);
  if (card) card.remove();

  showToast('❌', `Order #${order.id} cancelled. Page refreshing...`, 'warning');
  setTimeout(() => location.reload(), 2000);
}

// ─────────────────────────────────────────────
// Stat Card Counts (simple DOM update)
// ─────────────────────────────────────────────
function updateStatCounts() {
  const live      = document.querySelectorAll('#liveOrdersContainer .order-card').length;
  const completed = document.querySelectorAll('.order-card-delivered').length;
  const cancelled = document.querySelectorAll('.order-card-cancelled').length;
  const total     = live + completed + cancelled;

  const vals = document.querySelectorAll('.stat-value');
  if (vals[0]) vals[0].textContent = total;
  if (vals[1]) vals[1].textContent = live;
  if (vals[2]) vals[2].textContent = completed;
  if (vals[3]) vals[3].textContent = cancelled;
}

// ─────────────────────────────────────────────
// Add notification to the Notifications section
// ─────────────────────────────────────────────
function addNotifToDash(message) {
  const list = document.getElementById('notifListDash');
  const empty = list?.querySelector('.empty-state');
  if (empty) empty.remove();

  const item = document.createElement('div');
  item.className = 'notif-item-dash unread';
  item.style.animation = 'slideIn 0.3s ease';
  item.innerHTML = `
    <div class="notif-icon-dash"><i class="fas fa-bell"></i></div>
    <div class="notif-content-dash">
      <p class="notif-msg">${escapeHtml(message)}</p>
      <span class="notif-time">${new Date().toLocaleString()}</span>
    </div>
  `;
  list?.prepend(item);

  // Update nav badge
  const navBadge = document.querySelector('.nav-badge');
  if (navBadge) {
    navBadge.textContent = parseInt(navBadge.textContent || '0') + 1;
  }
}

// ─────────────────────────────────────────────
// Countdown Timers
// ─────────────────────────────────────────────
function updateCountdowns() {
  document.querySelectorAll('#liveOrdersContainer .order-card').forEach(card => {
    const status    = card.getAttribute('data-status');
    const placedStr = card.getAttribute('data-placed');
    const shippedStr = card.getAttribute('data-shipped');
    const timerEl   = card.querySelector('.countdown-timer');
    if (!timerEl) return;

    if (status === 'PLACED' || status === 'PAID') {
      if (!placedStr) return;
      const deadline = new Date(new Date(placedStr.replace('T',' ')).getTime() + 24 * 3600000);
      renderCountdown(timerEl, deadline);
    } else if (status === 'SHIPPED') {
      if (!shippedStr) return;
      const deadline = new Date(new Date(shippedStr.replace('T',' ')).getTime() + 48 * 3600000);
      renderCountdown(timerEl, deadline);
    } else {
      timerEl.textContent = '—';
    }
  });
}

function renderCountdown(el, deadline) {
  const diff = deadline - Date.now();
  if (diff <= 0) {
    el.textContent = 'Processing...';
    el.className = 'countdown-timer';
    return;
  }
  const h = Math.floor(diff / 3600000);
  const m = Math.floor((diff % 3600000) / 60000);
  const s = Math.floor((diff % 60000) / 1000);
  el.textContent = `${pad(h)}:${pad(m)}:${pad(s)}`;
  el.className = 'countdown-timer ' + (h < 2 ? 'urgent' : 'safe');
}

function pad(n) { return String(n).padStart(2, '0'); }

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
  const map = {
    ORDER_PLACED: 'info', PAYMENT_COMPLETED: 'success', ORDER_SHIPPED: 'warning',
    ORDER_DELIVERED: 'success', ORDER_CANCELLED: 'error', REMINDER: 'warning'
  };
  return map[type] || 'info';
}

// ─────────────────────────────────────────────
// Utilities
// ─────────────────────────────────────────────
function escapeHtml(str) {
  const d = document.createElement('div');
  d.appendChild(document.createTextNode(str || ''));
  return d.innerHTML;
}

function parseError(text) {
  try {
    const obj = JSON.parse(text);
    return obj.error || obj.message || text;
  } catch (_) {
    return text || 'Unknown error';
  }
}

// ─────────────────────────────────────────────
// Init
// ─────────────────────────────────────────────
document.addEventListener('DOMContentLoaded', () => {
  connectWebSocket();
  updateCountdowns();
  setInterval(updateCountdowns, 1000);
});
