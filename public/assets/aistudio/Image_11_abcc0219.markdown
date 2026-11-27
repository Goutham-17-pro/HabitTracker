---
name: LifeTrack
colors:
  surface: '#0f131d'
  surface-dim: '#0f131d'
  surface-bright: '#353944'
  surface-container-lowest: '#0a0e18'
  surface-container-low: '#171b26'
  surface-container: '#1c1f2a'
  surface-container-high: '#262a35'
  surface-container-highest: '#313540'
  on-surface: '#dfe2f1'
  on-surface-variant: '#bbcabf'
  inverse-surface: '#dfe2f1'
  inverse-on-surface: '#2c303b'
  outline: '#86948a'
  outline-variant: '#3c4a42'
  surface-tint: '#4edea3'
  primary: '#4edea3'
  on-primary: '#003824'
  primary-container: '#10b981'
  on-primary-container: '#00422b'
  inverse-primary: '#006c49'
  secondary: '#4cd7f6'
  on-secondary: '#003640'
  secondary-container: '#03b5d3'
  on-secondary-container: '#00424e'
  tertiary: '#d0bcff'
  on-tertiary: '#3c0091'
  tertiary-container: '#b090ff'
  on-tertiary-container: '#4600a7'
  error: '#ffb4ab'
  on-error: '#690005'
  error-container: '#93000a'
  on-error-container: '#ffdad6'
  primary-fixed: '#6ffbbe'
  primary-fixed-dim: '#4edea3'
  on-primary-fixed: '#002113'
  on-primary-fixed-variant: '#005236'
  secondary-fixed: '#acedff'
  secondary-fixed-dim: '#4cd7f6'
  on-secondary-fixed: '#001f26'
  on-secondary-fixed-variant: '#004e5c'
  tertiary-fixed: '#e9ddff'
  tertiary-fixed-dim: '#d0bcff'
  on-tertiary-fixed: '#23005c'
  on-tertiary-fixed-variant: '#5516be'
  background: '#0f131d'
  on-background: '#dfe2f1'
  surface-variant: '#313540'
typography:
  headline-xl:
    fontFamily: Plus Jakarta Sans
    fontSize: 40px
    fontWeight: '800'
    lineHeight: 48px
    letterSpacing: -0.03em
  headline-xl-mobile:
    fontFamily: Plus Jakarta Sans
    fontSize: 30px
    fontWeight: '800'
    lineHeight: 38px
    letterSpacing: -0.02em
  headline-lg:
    fontFamily: Plus Jakarta Sans
    fontSize: 32px
    fontWeight: '700'
    lineHeight: 40px
    letterSpacing: -0.02em
  headline-lg-mobile:
    fontFamily: Plus Jakarta Sans
    fontSize: 24px
    fontWeight: '700'
    lineHeight: 32px
    letterSpacing: -0.02em
  headline-md:
    fontFamily: Plus Jakarta Sans
    fontSize: 20px
    fontWeight: '600'
    lineHeight: 28px
    letterSpacing: -0.01em
  title-sm:
    fontFamily: Plus Jakarta Sans
    fontSize: 16px
    fontWeight: '600'
    lineHeight: 24px
  body-lg:
    fontFamily: Plus Jakarta Sans
    fontSize: 16px
    fontWeight: '400'
    lineHeight: 24px
  body-md:
    fontFamily: Plus Jakarta Sans
    fontSize: 14px
    fontWeight: '400'
    lineHeight: 20px
  body-sm:
    fontFamily: Plus Jakarta Sans
    fontSize: 12px
    fontWeight: '400'
    lineHeight: 16px
  label-md:
    fontFamily: Plus Jakarta Sans
    fontSize: 12px
    fontWeight: '600'
    lineHeight: 16px
    letterSpacing: 0.04em
  label-sm:
    fontFamily: Plus Jakarta Sans
    fontSize: 10px
    fontWeight: '700'
    lineHeight: 14px
    letterSpacing: 0.06em
rounded:
  sm: 0.25rem
  DEFAULT: 0.5rem
  md: 0.75rem
  lg: 1rem
  xl: 1.5rem
  full: 9999px
spacing:
  gutter: 1rem
  margin: 1rem
  space-xs: 0.25rem
  space-sm: 0.5rem
  space-md: 1rem
  space-lg: 1.5rem
  space-xl: 2rem
---

## Brand & Style

This design system establishes an ambient, high-performance executive dashboard for personal velocity. Built for ambitious students and modern professionals, it bridges the gap between structured productivity software and cognitive calm. The interface avoids clutter and overwhelming gamification, relying instead on clean modular surfaces, luminous visual feedback, and intentional negative space.

The aesthetic blends **Modern Tech Minimalism** with refined **Glassmorphism**. Deep slate canvases keep sensory fatigue minimal during early morning planning or late-night study sessions, while vibrant electric jewel tones create an immediate dopamine trigger when milestones and habit streaks are logged. The emotional response is focused, effortless, and premium—transforming daily discipline into a state of continuous flow.

## Colors

The palette revolves around deep atmospheric slate layers pierced by focused, luminous accents:

- **Primary (`#10B981` — Electric Emerald):** Denotes habit completions, active streaks, success indicators, and positive momentum.
- **Secondary (`#06B6D4` — Vivid Cyan):** Represents daily timeline metrics, routine sequencing, and circular progress fills.
- **Tertiary (`#8B5CF6` — Royal Violet):** Highlights high-priority tasks, deep work blocks, and focus sessions. Paired with an auxiliary Amber (`#F59E0B`) for critical alerts and urgent deadlines.
- **Neutral Dark Canvas (`#0B0F19` & `#111827`):** Forms the foundational viewport and secondary container surfaces, paired with structured border dividers (`#1E293B` and `#334155`).
- **Text & Foreground:** Pure crisp white (`#F8FAFC`) for primary headers, soft blue-slate (`#94A3B8`) for supporting metadata, and muted steel (`#64748B`) for disabled or tertiary captions.

## Typography

**Plus Jakarta Sans** provides a contemporary geometric presence with humanistic curves, delivering immediate scannability on high-density mobile screens.

- **Headlines:** Display metrics and progress headers leverage negative tracking (`-0.02em` to `-0.03em`) and heavy weights (`700`–`800`) to anchor dashboard hierarchy.
- **Body & Data Displays:** Body copy retains generous line height (`1.4`–`1.5`) for rapid readability when skimming scheduled routines.
- **Labels & Micro-Badges:** Metric pill tags and streak counters use uppercase or semi-bold weights with slight positive letter spacing (`0.04em`–`0.06em`) to ensure legibility at micro dimensions (10px–12px).

## Layout & Spacing

The layout is built for fluid mobile-first web viewports with an 8pt baseline rhythm:

- **Mobile Viewports (<640px):** Single-column stacked stream. Safe margin set to `1rem` (16px) with dynamic viewport bottom-padding (accounting for virtual navigation bars and home indicators). Sticky bottom action dock for logging actions.
- **Tablet / Large Mobile (640px–1024px):** 2-column asymmetric split (left rail for calendar timeline and habit checklist; right rail for circular completion metrics, streak analytics, and focus timers).
- **Desktop Dashboards (>1024px):** Max container width clamped to `1200px` centered with 12-column grid alignment, `1.5rem` gutters, and persistent sidebar navigation.
- **Density:** Routine check items utilize compact vertical padding (`space-sm` to `space-md`) to ensure that at least 5–7 routine items remain visible above the fold on mobile screens without scrolling.

## Elevation & Depth

Visual hierarchy uses a refined layered dark substrate model rather than stark dropshadows:

1. **Base Layer (Canvas):** Pure `#0B0F19`. Zero elevation.
2. **Surface Layer (Cards & Modules):** `#111827` overlaid with a subtle 1px border (`#1E293B`).
3. **Floating Glass Elements (Sticky Action Bars & Modals):** Semi-transparent `#111827` at `80%` opacity with a `12px` to `16px` backdrop-blur filter and an upper inner light rim (`rgba(255, 255, 255, 0.08)`).
4. **Active State Glows (Luminescence):** Elements receiving user focus or reaching 100% habit completion gain a soft, tinted ambient outer glow (`0 0 20px rgba(16, 185, 129, 0.25)`) instead of traditional drop shadows.

## Shapes

The geometric identity balances precision with welcoming, tactile curvature:

- **Containers & Cards:** Utilize standard roundedness (`0.5rem` / 8px for micro cards, up to `1rem` / 16px for primary modules) to create clear modular boundaries.
- **Interactive Badges, Chips & Progress Indicators:** Utilize fully circular or pill geometry (`9999px`) to visually separate transient status labels from persistent structural cards.
- **Circular Data Rings:** Progress rings for daily completion and hydration/steps are rendered with uniform stroke widths and rounded cap endings.

## Components

### Buttons
- **Primary Action (Check-in / Complete):** Solid Electric Emerald (`#10B981`) with rich slate text (`#0B0F19`), font weight `700`, pill radius (`9999px`). Active state exhibits subtle scale contraction (`0.98`).
- **Secondary Action (Edit / Filter):** Transparent surface with `#1E293B` border, text in `#F8FAFC`, transitioning to `#334155` border on hover.
- **Ghost Action:** Borderless, slate-colored text (`#94A3B8`) hoverable to white (`#F8FAFC`).

### Habit & Routine Cards
- Encased in `#111827` with a 1px border (`#1E293B`).
- Contains a quick-toggle checkbox on the leading edge, task title and schedule category in the center, and current streak badge (`🔥 12d`) on the trailing edge.
- Completed state shifts card background to a muted tint (`rgba(16, 185, 129, 0.05)`) with an emerald check indicator.

### Circular Progress Rings
- SVG-based circular tracks with a dark base trail (`#1E293B`) and dynamic stroke-dashoffset colored via an Emerald-to-Cyan linear gradient.
- Central typography displays percentage or remaining items (e.g., `4/6 Done`).

### Chips & Badges
- Pill-shaped (`9999px`) metadata badges.
- Category designations use low-opacity tinted backgrounds with high-contrast text (e.g., Purple badge: background `rgba(139, 92, 246, 0.15)`, text `#C4B5FD`).

### Checkboxes & Habit Toggles
- Custom circular targets (24px × 24px) for easy thumb tapping. Unchecked states display an empty ring (`#334155`). Checking triggers a spring-animated fill into `#10B981` with a white checkmark.

### Input Fields
- Dark embedded field background (`#0B0F19`), 1px structural border (`#1E293B`), text in `#F8FAFC`, placeholder in `#64748B`.
- Focus state highlights border in `#06B6D4` with an ambient glow (`box-shadow: 0 0 0 3px rgba(6, 182, 212, 0.15)`).

### Focus Timer / Streak Sheet
- Glassmorphic modal drawer with grab bar for mobile thumb access, showcasing deep work countdowns and detailed streak history graphs.