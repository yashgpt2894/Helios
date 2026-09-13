# Droid Workflow

```svg
<svg xmlns="http://www.w3.org/2000/svg" width="900" height="300" viewBox="0 0 900 300">
  <defs>
    <marker id="arrowhead" markerWidth="10" markerHeight="7" refX="9" refY="3.5" orient="auto">
      <polygon points="0 0, 10 3.5, 0 7" fill="#94a3b8"/>
    </marker>
    <filter id="shadow" x="-10%" y="-10%" width="120%" height="130%">
      <feDropShadow dx="0" dy="4" stdDeviation="4" flood-color="#000000" flood-opacity="0.08"/>
    </filter>
  </defs>

  <!-- Background -->
  <rect width="900" height="300" fill="#0f172a" rx="12"/>

  <!-- Title -->
  <text x="450" y="42" text-anchor="middle" font-family="-apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif" font-size="18" font-weight="600" fill="#f1f5f9">
    Droid Workflow
  </text>
  <text x="450" y="64" text-anchor="middle" font-family="-apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif" font-size="12" fill="#64748b">
    Orchestrated by /missions
  </text>

  <!-- Stage 1: Mission -->
  <g transform="translate(80, 120)">
    <rect width="160" height="80" rx="12" fill="#1e293b" stroke="#3b82f6" stroke-width="2" filter="url(#shadow)"/>
    <text x="80" y="35" text-anchor="middle" font-family="-apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif" font-size="14" font-weight="600" fill="#3b82f6">MISSION</text>
    <text x="80" y="58" text-anchor="middle" font-family="-apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif" font-size="11" fill="#94a3b8">Define objective &amp;</text>
    <text x="80" y="74" text-anchor="middle" font-family="-apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif" font-size="11" fill="#94a3b8">scope via /missions</text>
  </g>

  <!-- Arrow 1 -->
  <line x1="240" y1="160" x2="300" y2="160" stroke="#334155" stroke-width="2" marker-end="url(#arrowhead)"/>

  <!-- Stage 2: Planning -->
  <g transform="translate(300, 120)">
    <rect width="160" height="80" rx="12" fill="#1e293b" stroke="#8b5cf6" stroke-width="2" filter="url(#shadow)"/>
    <text x="80" y="35" text-anchor="middle" font-family="-apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif" font-size="14" font-weight="600" fill="#8b5cf6">PLANNING</text>
    <text x="80" y="58" text-anchor="middle" font-family="-apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif" font-size="11" fill="#94a3b8">Planner droid maps</text>
    <text x="80" y="74" text-anchor="middle" font-family="-apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif" font-size="11" fill="#94a3b8">milestones &amp; tasks</text>
  </g>

  <!-- Arrow 2 -->
  <line x1="460" y1="160" x2="520" y2="160" stroke="#334155" stroke-width="2" marker-end="url(#arrowhead)"/>

  <!-- Stage 3: Building -->
  <g transform="translate(520, 120)">
    <rect width="160" height="80" rx="12" fill="#1e293b" stroke="#10b981" stroke-width="2" filter="url(#shadow)"/>
    <text x="80" y="35" text-anchor="middle" font-family="-apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif" font-size="14" font-weight="600" fill="#10b981">BUILDING</text>
    <text x="80" y="58" text-anchor="middle" font-family="-apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif" font-size="11" fill="#94a3b8">Builder droid writes</text>
    <text x="80" y="74" text-anchor="middle" font-family="-apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif" font-size="11" fill="#94a3b8">code &amp; runs tests</text>
  </g>

  <!-- Arrow 3 -->
  <line x1="680" y1="160" x2="740" y2="160" stroke="#334155" stroke-width="2" marker-end="url(#arrowhead)"/>

  <!-- Stage 4: Reviewer -->
  <g transform="translate(740, 120)">
    <rect width="160" height="80" rx="12" fill="#1e293b" stroke="#f59e0b" stroke-width="2" filter="url(#shadow)"/>
    <text x="80" y="35" text-anchor="middle" font-family="-apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif" font-size="14" font-weight="600" fill="#f59e0b">REVIEWER</text>
    <text x="80" y="58" text-anchor="middle" font-family="-apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif" font-size="11" fill="#94a3b8">Reviewer droid audits</text>
    <text x="80" y="74" text-anchor="middle" font-family="-apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif" font-size="11" fill="#94a3b8">quality &amp; correctness</text>
  </g>

  <!-- Optional feedback loop (dashed) -->
  <path d="M 820 200 Q 820 240 450 240 Q 160 240 160 200" fill="none" stroke="#475569" stroke-width="2" stroke-dasharray="6,4" marker-end="url(#arrowhead)"/>
  <text x="450" y="258" text-anchor="middle" font-family="-apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif" font-size="11" fill="#64748b" font-style="italic">Feedback loop (if issues found)</text>

  <!-- Legend / Droids -->
  <g transform="translate(80, 280)">
    <circle cx="6" cy="6" r="4" fill="#3b82f6"/>
    <text x="18" y="10" font-family="-apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif" font-size="10" fill="#64748b">mission orchestrator</text>
    <circle cx="130" cy="6" r="4" fill="#8b5cf6"/>
    <text x="142" y="10" font-family="-apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif" font-size="10" fill="#64748b">planner droid</text>
    <circle cx="240" cy="6" r="4" fill="#10b981"/>
    <text x="252" y="10" font-family="-apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif" font-size="10" fill="#64748b">builder droid</text>
    <circle cx="350" cy="6" r="4" fill="#f59e0b"/>
    <text x="362" y="10" font-family="-apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif" font-size="10" fill="#64748b">reviewer droid</text>
  </g>
</svg>

```