const SITES = [
  { key: 'tiktok',    label: 'TikTok',    domain: 'tiktok.com' },
  { key: 'instagram', label: 'Instagram', domain: 'instagram.com' },
  { key: 'youtube',   label: 'YouTube',   domain: 'youtube.com' },
  { key: 'twitter',   label: 'Twitter / X', domain: 'twitter.com & x.com' },
  { key: 'facebook',  label: 'Facebook',  domain: 'facebook.com' },
  { key: 'reddit',    label: 'Reddit',    domain: 'reddit.com' },
];

// Render site checkboxes
const siteList = document.getElementById('site-list');
SITES.forEach(({ key, label, domain }) => {
  const el = document.createElement('label');
  el.className = 'site-item';
  el.innerHTML = `
    <input type="checkbox" class="site-check" data-key="${key}" />
    <span class="site-info">
      <span class="site-name">${label}</span>
      <span class="site-domain">${domain}</span>
    </span>
  `;
  siteList.appendChild(el);
});

// Load saved settings
browser.storage.local.get(['targetSites', 'mode']).then(({ targetSites, mode }) => {
  const enabled = targetSites || SITES.map(s => s.key);
  document.querySelectorAll('.site-check').forEach((cb) => {
    cb.checked = enabled.includes(cb.dataset.key);
  });
  const val = mode || 'normal';
  const radio = document.querySelector(`input[name="mode"][value="${val}"]`);
  if (radio) radio.checked = true;
});

// Save
document.getElementById('btn-save').addEventListener('click', () => {
  const targetSites = [...document.querySelectorAll('.site-check')]
    .filter(cb => cb.checked)
    .map(cb => cb.dataset.key);

  const modeRadio = document.querySelector('input[name="mode"]:checked');
  const mode = modeRadio?.value || 'normal';

  browser.storage.local.set({ targetSites, mode }).then(() => {
    const msg = document.getElementById('saved-msg');
    msg.classList.remove('hidden');
    setTimeout(() => msg.classList.add('hidden'), 2000);
  });
});
