# Research: Doomscrolling & Sleep

Comprehensive background research for the sleep-scroll project.

---

## Table of Contents

1. [The Scale of the Problem](#1-the-scale-of-the-problem)
2. [The Core Insight: It's Not the Screen, It's the Content](#2-the-core-insight-its-not-the-screen-its-the-content)
3. [Why We Can't Stop: The Neurobiological Trap](#3-why-we-cant-stop-the-neurobiological-trap)
4. [Platform Design: Built to Keep You Up](#4-platform-design-built-to-keep-you-up)
5. [Mechanisms of Sleep Disruption](#5-mechanisms-of-sleep-disruption)
6. [Blue Light: The Overrated Culprit](#6-blue-light-the-overrated-culprit)
7. [Health Consequences of Sleep Deprivation](#7-health-consequences-of-sleep-deprivation)
8. [Who Is Most at Risk](#8-who-is-most-at-risk)
9. [What Actually Works: Interventions](#9-what-actually-works-interventions)
10. [Evidence for the Wind-Down Styles](#10-evidence-for-the-wind-down-styles)
11. [Evidence Ranking: Core Design Choices](#11-evidence-ranking-core-design-choices)
12. [Design Principles for Sleep-Scroll](#12-design-principles-for-sleep-scroll)
13. [Sources](#13-sources)

---

## 1. The Scale of the Problem

### Doomscrolling Prevalence
- **38% of U.S. adults** say using their phone for news/current events before bed makes their sleep worse; rises to **46% among 18–24 year olds** (AASM)
- **80% of U.S. adults** report losing sleep because of social media use; **93% of Gen Z** (18–25) admit staying up past bedtime due to social media (AASM)
- Social media now **dominates pre-bedtime routines** for U.S. adults, surpassing reading or watching TV

### Global Sleep Deprivation Crisis
- Insomnia affects at least **3 in 10 people worldwide**; chronic insomnia affects **10–30% of adults** globally
- The U.S. economy loses **$63 billion per year** due to insomnia-related lost productivity (workers lose ~11.3 productive days/year)
- Developed nations (UK, Japan, Germany) face sleep deprivation costing **1–3% of GDP annually**
- The CDC has declared insufficient sleep a **public health epidemic**
- Most adolescents need 8–10 hours but consistently fall short; ~**two-thirds of young adults** regularly get less than the recommended 7–9 hours

---

## 2. The Core Insight: It's Not the Screen, It's the Content

**This is the most important finding for product design.**

A landmark 2024 study of 830 young adults found that **frequent social media visits and emotional investment were stronger predictors of poor sleep than total screen time**. Simply reducing hours on a device is not enough.

The three factors that actually drive sleep disruption:
1. **How often** you check (check frequency > duration)
2. **How emotionally invested** you are in what you see
3. **What kind of content** you're consuming (anxiety-inducing > neutral)

> "Internalizing topics that are stressful or worrisome before bed makes it difficult to have the deep, restorative sleep that is imperative to overall health." — Dr. James Rowley, AASM Past President

**Key peer-reviewed evidence:**
- **Levenson et al. (2017), *Sleep*, Oxford Academic.** Nationally representative sample of 1,763 US young adults (19–32). Those who checked social media "often or very often" in the 30 minutes before bed had **1.62× the odds of sleep disturbance** vs. those who rarely checked (adjusted OR = 1.62, 95% CI = 1.31–2.34). This was the *frequency* metric, controlling for total daily use.
- **UPMC / Primack Lab.** "Frequency of social media visits is a better predictor of sleep difficulty than overall time spent." Greater emotional investment was independently associated with greater insomnia severity.
- **MDPI *Behavioral Sciences* (2024), Vol. 14(9).** Pre-sleep cognitive arousal and social comparison mediated the relationship between emotional engagement and poor sleep in US emerging adults.

**Implication:** A tool that cuts screen time by 30 minutes won't work as well as one that reduces the emotional intensity of the last 30 minutes of use.

---

## 3. Why We Can't Stop: The Neurobiological Trap

### Dopamine and Variable Reward Schedules

Social media platforms function as **slot machines** — a deliberate design choice, not an accident.

- Slot machines use **variable ratio reinforcement schedules** (unpredictable rewards), which produce the most resistant habits of any reward schedule known to behavioral science
- **Pull-to-refresh is mechanically identical to pulling a lever.** The gesture was explicitly modeled on slot machine mechanics
- Likes, comments, and notifications arrive unpredictably — the most powerful form of variable reinforcement. The *anticipation* of an unpredictable reward triggers dopamine release, often more powerfully than the reward itself
- Neuroimaging studies show that social media interaction (especially "liking") significantly activates the **striatum** — the core dopamine reward region — with activation intensity dose-dependently correlated with subjective pleasure

### Dopamine Depletion
- Users who spend more phone time on social apps show **measurably lower dopamine synthesis capacity** in brain regions involved in reinforcement learning and habit formation (2025 research)
- This is not just behavioral — it is a **measurable neurobiological change**

### Dissociation and "Brain Rot"
- The infinite feed induces **normative dissociation** — an absorbed mental state characterized by loss of awareness and disrupted memory, causing users to be unable to recall what they just read
- This absorbed state is why sessions last far longer than intended: the user has lost the thread of time
- Research identifies this as "brain rot": emotional desensitization, cognitive overload, and negative self-concept from chronic overuse

### The Addiction Profile
- A 2024–2025 literature review classified doomscrolling as a **behavioral addiction** with measurable links to: impulsivity, depression, future anxiety, risky behavior, lower life satisfaction, and reduced motivation
- A two-wave longitudinal study confirmed the **bidirectional relationship** between doomscrolling and social media addiction in adolescents — each reinforces the other

---

## 4. Platform Design: Built to Keep You Up

These features are not neutral — they are **engineered attention-capture systems**:

| Feature | Mechanism | Effect |
|---|---|---|
| Infinite scroll | Removes natural stopping points | Sessions extend indefinitely |
| Pull-to-refresh | Variable reward slot machine gesture | Compulsive checking behavior |
| Autoplay (video) | Removes the active choice to continue | Passive consumption by default |
| Notification badges | Unpredictable variable rewards | Habitual phone checking |
| Algorithmic feed | Surfaces emotionally provocative content | Heightened arousal; outrage prioritized |
| "Seen by" / read receipts | Social pressure and anxiety | Prevents disengagement |

The **Social Media Addiction Reduction Technology (SMART) Act** (U.S. federal legislation) specifically proposed banning infinite scroll and autoplay, and setting default usage limits — acknowledging these are not neutral design choices.

Research classifies infinite scroll as a **dark pattern**: a design intended to manipulate users into actions contrary to their own interests.

---

## 5. Mechanisms of Sleep Disruption

Five pathways from scrolling to poor sleep, ranked by research strength:

### 1. Pre-sleep Cognitive Arousal (strongest evidence)
Emotionally charged content keeps the brain in **heightened alertness**, delaying sleep onset. News, political content, and social conflict are particularly potent triggers. This is a core mechanism of insomnia — hyperarousal — applied externally by platform content.

### 2. Habitual / Automatic Checking
After-lights-out phone use becomes an **automatic behavior** divorced from conscious intent. The habit loop (cue → routine → reward) is reinforced nightly. Breaking it requires either friction or replacement — awareness alone is insufficient.

### 3. Social Comparison
Viewing curated, idealized, or distressing posts before bed triggers **upward social comparison**, increasing pre-sleep cognitive arousal through stress and feelings of inadequacy. This independently predicts worse sleep quality and greater insomnia severity.

### 4. Existential Anxiety
A 2024 study (n=800, *Computers in Human Behavior Reports*) specifically identified **existential anxiety** as a unique doomscrolling outcome — a deeper dread response, distinct from ordinary stress or news fatigue, triggered by consuming content about systemic threats.

### 5. Displaced Sleep Time
Even absent emotional effects, scrolling **directly displaces sleep**. Time spent on the phone after lights out is simply time not sleeping. The Sleep Foundation found screen users before bed slept ~**50 fewer minutes per week** and had a **33% higher rate of poor sleep quality**.

---

## 6. Blue Light: The Overrated Culprit

Blue light is real, but overstated as the *primary* cause.

**What's true:**
- Blue light (~400–500 nm) suppresses melatonin production and delays circadian phase
- 2 hours of evening exposure caused an average **1.1-hour circadian phase delay** (2021 study on university students)
- Blue light stimulates parts of the brain associated with alertness, raising arousal independent of content

**What's nuanced:**
- Newer research (Canada, 2025) finds blue light before bed may not be as harmful for adults as previously believed — overall sleep health similar between nightly screen users and non-users when controlling for content type
- A 2023 study found melatonin suppression effects are largely mitigated **if the phone is put away at least 1 hour before bed**
- **Adolescents and teens are more vulnerable**: puberty increases light sensitivity, making blue light a real risk for younger users even when content is controlled

**Practical upshot:** Grayscale mode and night shift settings are worth offering, but don't market the product as a blue light solution — the evidence doesn't support that framing, and it undersells the real problem (emotional arousal from content).

---

## 7. Health Consequences of Sleep Deprivation

### Mental Health
- Bidirectional relationship: poor sleep worsens mental health, which worsens sleep (reinforcing loop)
- Associated with: depression, anxiety, suicidal ideation, negative mood, impaired emotional regulation, psychosis risk
- Physiologically: elevated cortisol, disrupted circadian rhythm, decreased neurogenesis, impaired brain plasticity
- Sleep under 7 hours: **6–15% increased risk of all-cause mortality**

### Cardiovascular
- Increased risk of hypertension, coronary artery disease, stroke, arrhythmia, heart failure
- Sleep deprivation interferes with glucose metabolism → insulin resistance → increased diabetes risk
- Meta-analysis: consistent association between sleep deprivation and cardiovascular disease across epidemiological and experimental studies

### Immune System
- Reduces production of infection-fighting cytokines and antibodies
- Creates a **chronic inflammatory state**
- Increases susceptibility to infectious diseases and raises risk for cardiometabolic, autoimmune, and neurodegenerative conditions

### Cognitive and Behavioral
- Impaired memory consolidation, attention, and decision-making
- Worse academic and professional performance
- Employees who doomscroll during work hours show **reduced work engagement** (2024 study, *Computers in Human Behavior*)
- Increased impulsivity and risk-taking

---

## 8. Who Is Most at Risk

| Group | Primary Risk Factors | Notes |
|---|---|---|
| Teens (13–17) | Blue light sensitivity + emotional arousal + addiction loop | Highest vulnerability; still developing; puberty amplifies light sensitivity |
| Young adults (18–25) | Habitual checking, FOMO, social comparison, addiction | 93% of Gen Z affected; most research-documented group |
| Adults (26–44) | News/current events anxiety, stress spillover from work | Existential anxiety pathway most relevant |
| Night shift workers | Disrupted circadian baseline + screen use | Compounded risk |
| People with anxiety/depression | Bidirectional reinforcement loop | Scrolling worsens condition; condition drives more scrolling |

---

## 9. What Actually Works: Interventions

Evidence-based ranking from strongest to weakest effect:

### High Evidence
| Intervention | Effect | Source |
|---|---|---|
| **Phone physically off the nightstand** | Strong | AASM clinical recommendation; breaks grab reflex |
| **No social media 30–60 min before bed** | Strong | AASM; melatonin recovery begins within this window |
| **Reducing check frequency** (not just duration) | Strong | Levenson et al. 2017 (*Sleep*); UPMC Primack Lab |
| **Replacement wind-down ritual** | Strong | HABITs RCT (*Trials*, 2024); JMIR mHealth meta-analysis 2022 |
| **Breathing exercises before sleep** | Strong | PMC8546813 RCT; *Scientific Reports* polysomnography 2020 |
| **Gradual friction (phased approach)** | Strong | Wood & Neal 2016 (*Behav. Sci. Policy*); PMC10050503 systematic review |

### Moderate Evidence
| Intervention | Effect | Source |
|---|---|---|
| **Active nudges / in-app friction** | Moderate–Strong | *Frontiers in Psychiatry* PMC12310694 (2025); outperformed hard blockers |
| **Mindfulness before sleep** | Moderate–Strong | PubMed 29441644; *Journal of Sleep Research* 2022 |
| **Gratitude / reflection journaling** | Moderate | Wood et al. PubMed 19073292; Scullin et al. 2018 PMC5758411 |
| **Reducing emotional investment** | Moderate | Unfollow/mute anxiety-inducing content; 2024 study confirmed |
| **Grayscale / night mode** | Moderate (teens) | Meaningful for under-18 users; modest for adults |

### Lower/Mixed Evidence
| Intervention | Effect | Source |
|---|---|---|
| **Hard time limits / screen time caps** | Mixed | Rarely produce actual reduction alone; often bypassed — PMC9066336 |
| **Full digital detox / cold-turkey** | Mixed | 78% relapse within 10 days; desire-regenerating effect — *Eur. J. Marketing* 2024 |
| **Awareness-only tools** (just showing usage stats) | Weak | Knowledge alone does not change behavior — Wood & Neal 2016 |

### Why hard blocks specifically fail
- **Wood & Neal (2016), *Behavioral Science & Policy*.** Implementation intentions (if-then plans) are not successful at controlling strong, well-established habits. Long-term change requires disrupting the cue-routine association, not blocking the reward.
- **Digital detox and the app-blocking app (*European Journal of Marketing*, 2024).** Abstinence-only approaches act as a **desire-regenerating force** — blocking access temporarily intensifies the urge and leads to binge-like rebounds.
- **Users prefer autonomy.** Self-nudging RCT (SSRN, 2025): users prefer nudge-based tools over hard blockers because they retain a sense of control. Autonomy preservation predicts sustained use.

### Why gradual friction specifically works
- **PMC10050503 (2023).** Gradual modifications to reinforcement schedules consistently outperform abrupt removal. Core mechanism: prevents the *contrast effect* — the sharp deprivation response that drives rebellion and relapse.
- **PMC11161714 (2024).** Graded task difficulty builds self-efficacy — users who succeed at smaller friction steps believe the harder steps are reachable.

### Key Compliance Challenge
A 2025 randomized controlled study found that when students successfully practiced bedtime digital detox, they felt better the next day — but compliance was only **55.57% of days**. The gap between knowing and doing is the core product problem. Gradual friction keeps the user inside the tool rather than requiring them to opt in each night.

---

## 10. Evidence for the Wind-Down Styles

sleep-scroll offers five phase-3 wind-down styles. Each is grounded in research:

| Style | Research basis |
|---|---|
| **Breathing guide** | PMC8546813 RCT: significant improvement in sleep quality, latency, efficiency at 1 and 3 months. *Sci. Reports* 2020: single 20-min slow breathing session reduced sleep-onset latency and night awakenings (polysomnography). |
| **Reflection / gratitude** | Wood et al. (PubMed 19073292): gratitude predicted greater sleep quality, longer duration, less latency. Mechanism: increases positive pre-sleep cognitions, reduces negative rumination. |
| **Journal prompt** | Scullin et al. 2018 (PMC5758411): only polysomnographic study of bedtime writing. Writing a specific to-do list for 5 minutes caused significantly faster sleep onset vs. journaling completed activities. Writing "offloads" cognitive preoccupation. |
| **Ambient sounds (prompt)** | No direct audio generation (Web Audio API requires user gesture to activate). Positioned as a prompt to start calming audio the user already knows they like. |
| **Silence** | Absence of arousal. No RCT, but rationale is strong: zero cognitive load is optimal for users with high pre-sleep arousal who need no additional task. |

### Why we ask "how do you wind down?" not "what is your religion?"

sleep-scroll personalises the wind-down experience without collecting religious data. This is intentional:

1. **Legal risk.** Religion is a GDPR Special Category item (Article 9) — collecting it requires explicit legal justification and carries higher breach liability.
2. **Unnecessary.** Asking *"what calming behavior works for you?"* achieves identical personalization. A Muslim user selects "reflection"; a Christian user may select the same; a secular user selects breathing or silence. The product never infers or stores religious identity — only behavioral preference.
3. **Drop-off risk.** Onboarding surveys asking about religion have higher abandonment rates than behavioral preference questions.
4. **Research alignment.** Replacement behavior research shows substitution is more effective when the new behavior has personal resonance — behavioral preference captures this without requiring the sensitive data.

---

## 11. Evidence Ranking: Core Design Choices

| Design choice | Key source | Strength |
|---|---|---|
| Target check frequency, not total time | Levenson et al. 2017 (*Sleep*) | ★★★★★ |
| Gradual friction over hard blocks | Wood & Neal 2016; PMC10050503 | ★★★★★ |
| Breathing guide as wind-down | PMC8546813 RCT; *Sci. Reports* 2020 | ★★★★★ |
| Journaling / to-do offloading for sleep onset | Scullin et al. 2018 PMC5758411 | ★★★★★ |
| Replacement behavior at wind-down | HABITs RCT 2024; JMIR mHealth 2022 | ★★★★☆ |
| Emotional arousal > blue light | PubMed 34627122; Almeida 2023 | ★★★★☆ |
| Nudging over blocking | PMC12310694 2025; PMC9066336 2022 | ★★★★☆ |
| Doomscrolling as anxiety-reduction loop | PMC9580444; PMC8250995 | ★★★★☆ |
| Gratitude / reflection prompts | Wood et al. PubMed 19073292 | ★★★★☆ |

---

## 12. Design Principles for Sleep-Scroll

Derived from the research above:

1. **Target emotional engagement, not just time.** The product should reduce anxiety-inducing content exposure in the pre-sleep window, not simply cut minutes.

2. **Create friction, not walls.** Hard blocks get bypassed. Strategic friction (a pause, a question, a confirmation) increases mindfulness without destroying user experience. But friction must be calibrated — too heavy creates frustration.

3. **Replace, don't remove.** Habit loops require a substitute behavior. Offer a wind-down alternative when blocking or warning, not just a stop.

4. **Attack check frequency.** One intervention that matters: make the first pick-up after lights-out harder, not just the duration of each session.

5. **Time-specific, not all-day.** Research shows time-specific detox (bedtime window) is more effective and more acceptable to users than blanket restrictions.

6. **Younger users need different design.** Teens require more protection (blue light + emotional arousal + addiction vulnerability). Adults respond better to autonomy-preserving nudges.

7. **Make the invisible visible.** Users in dissociative scroll states are unaware of time passing. Ambient time awareness (non-intrusive) breaks the dissociation without creating jarring interruptions.

8. **Don't use guilt.** Research on doomscrolling shows it's already associated with shame and self-blame. Tools that reinforce guilt reduce engagement without improving outcomes.

---

## 13. Sources

### Doomscrolling & Sleep
- [AASM: Americans are doomscrolling at bedtime](https://aasm.org/americans-are-doomscrolling-at-bedtime-prioritizing-screen-time-over-sleep)
- [AASM: 93% of Gen Z stay up past bedtime due to social media](https://aasm.org/are-you-tiktok-tired-93-of-gen-z-admit-to-staying-up-past-their-bedtime-due-to-social-media/)
- [The Conversation: Screens aren't the main culprit](https://theconversation.com/social-media-before-bedtime-wreaks-havoc-on-our-sleep-a-sleep-researcher-explains-why-screens-alone-arent-the-main-culprit-251453)
- [Scientific American: Why Social Media Screen Time Is So Bad for Sleep](https://www.scientificamerican.com/article/why-social-media-screen-time-is-so-bad-for-sleep/)
- [Harvard Health: Doomscrolling dangers](https://www.health.harvard.edu/mind-and-mood/doomscrolling-dangers)
- [PMC: Social Media Use and Sleep — Scoping Review (2024)](https://pmc.ncbi.nlm.nih.gov/articles/PMC10948475/)
- [PMC: Social Media Use Before Bed and Sleep Disturbance](https://pmc.ncbi.nlm.nih.gov/articles/PMC8205627/)
- [PMC: Social Media Use and Sleep Disturbance in Adolescents](https://pmc.ncbi.nlm.nih.gov/articles/PMC8233562/)
- [MDPI: Mechanisms Linking Social Media Use and Sleep in Emerging Adults](https://www.mdpi.com/2076-328X/14/9/794)
- [Sleep Foundation: Social Media and Sleep](https://www.sleepfoundation.org/how-sleep-works/sleep-and-social-media)
- [Sleep Foundation: Social Media Dominates Pre-Bedtime Routine](https://www.sleepfoundation.org/sleep-news/how-does-social-media-affect-sleep)

### Neuroscience & Behavioral Addiction
- [PMC: Dopamine-scrolling as a public health challenge (2025)](https://pmc.ncbi.nlm.nih.gov/articles/PMC12322333/)
- [PMC: Doomscrolling Scale — Personality, Distress, Wellbeing](https://pmc.ncbi.nlm.nih.gov/articles/PMC9580444/)
- [PMC: Brain Rot in the Digital Era — Review (2025)](https://pmc.ncbi.nlm.nih.gov/articles/PMC11939997/)
- [PMC: Emotional Reinforcement Mechanisms in Social Media Addiction (2025)](https://pmc.ncbi.nlm.nih.gov/articles/PMC12108933/)
- [ScienceDirect: Reward Variability and Behavioral Addiction](https://www.sciencedirect.com/science/article/pii/S0306460323000217)
- [University of Michigan: Social media copies gambling methods](https://ihpi.umich.edu/news/social-media-copies-gambling-methods-create-psychological-cravings)
- [ResearchGate: Doomscrolling and Social Media Addiction — Longitudinal Study](https://www.researchgate.net/publication/391841469_Doomscrolling_and_social_media_addiction_in_adolescents_a_two-wave_longitudinal_study)

### Platform Design & Dark Patterns
- [ACM: Purpose Mode — Reducing Attention Capture Dark Patterns](https://dl.acm.org/doi/10.1145/3711841)
- [arXiv: Design Frictions on Social Media (2024)](https://arxiv.org/abs/2407.18803)
- [arXiv: Scrolling in the Deep — Intervention Effectiveness](https://arxiv.org/html/2501.11814v1)
- [arXiv: Infinite Scrolling and User Satisfaction](https://arxiv.org/html/2408.09601v1)
- [Weizenbaum: Dark Patterns and Addictive Designs](https://ojs.weizenbaum-institut.de/index.php/wjds/article/view/5_3_2/189)

### Blue Light & Circadian Rhythm
- [PMC: Blue Light — Circadian Rhythm and Cognitive Functions (2024)](https://pmc.ncbi.nlm.nih.gov/articles/PMC11252550/)
- [PMC: Blue Light Influence on Sleep, Performance, and Wellbeing](https://pmc.ncbi.nlm.nih.gov/articles/PMC9424753/)
- [PMC: Smartphone Use and Circadian Rhythm — Short-Wavelength Light](https://pmc.ncbi.nlm.nih.gov/articles/PMC7838958/)
- [Sleep Foundation: Blue Light and Sleep](https://www.sleepfoundation.org/bedroom-environment/blue-light)
- [Time: Blue Light Before Bed May Not Be So Bad (2025)](https://time.com/7335087/doom-scroll-phone-night-melatonin/)

### Health Consequences
- [PMC: Sleep Deprivation — Physical and Mental Health Outcomes (2025)](https://journals.sagepub.com/doi/10.1177/15598276251346752)
- [PMC: Sleep Deprivation and Cardiovascular Disease Risk](https://pmc.ncbi.nlm.nih.gov/articles/PMC10565718/)
- [PMC: Sleep Deprivation and Immune-Related Disease Risk](https://pmc.ncbi.nlm.nih.gov/articles/PMC8602722/)
- [PMC: Sleep Deprivation — Post-COVID Frontiers (2025)](https://pmc.ncbi.nlm.nih.gov/articles/PMC11922443/)

### Interventions & Design
- [PMC: Active Nudging for Digital Wellbeing and Sleep (2025)](https://pmc.ncbi.nlm.nih.gov/articles/PMC12310694/)
- [ScienceDirect: Digital Detox RCT — Reducing Smartphone Usage (2025)](https://www.sciencedirect.com/science/article/pii/S0747563225000718)
- [APA: Time-Specific Digital Detox Interventions — College Students](https://tmb.apaopen.org/pub/0ikby46e/release/1)
- [ScienceDirect: MinimalistPhone App — Behavior and Emotional Experience (2025)](https://www.sciencedirect.com/science/article/pii/S2451958825001149)
- [Frontiers: Reducing Excessive Screen Time — Nudges and Sleep Quality (2025)](https://www.frontiersin.org/journals/psychiatry/articles/10.3389/fpsyt.2025.1602997/full)
- [SAGE: Digital Detox — Systematic Literature Review](https://journals.sagepub.com/doi/10.1177/20501579211028647)

### Peer-Reviewed Studies (core citations)
- [Levenson et al. (2017) — Social media before bed and sleep disturbance (*Sleep*, Oxford)](https://academic.oup.com/sleep/article/40/9/zsx113/3926043)
- [Wood & Neal (2016) — Healthy through Habit (*Behavioral Science & Policy*)](https://journals.sagepub.com/doi/abs/10.1177/237946151600200109)
- [Scullin et al. (2018) — Bedtime writing and sleep onset (PMC5758411)](https://pmc.ncbi.nlm.nih.gov/articles/PMC5758411/)
- [Breathing + sleep RCT (PMC8546813)](https://pmc.ncbi.nlm.nih.gov/articles/PMC8546813/)
- [Pre-sleep slow breathing polysomnography (*Sci. Reports* 2020)](https://www.nature.com/articles/s41598-020-64218-7)
- [HABITs sleep intervention RCT (*Trials* 2024)](https://link.springer.com/article/10.1186/s13063-024-08599-4)
- [Gratitude and sleep quality — Wood et al. (PubMed 19073292)](https://pubmed.ncbi.nlm.nih.gov/19073292/)
- [Doomscrolling Scale (PMC9580444)](https://pmc.ncbi.nlm.nih.gov/articles/PMC9580444/)
- [Doomscrolling during COVID-19 (PMC8250995)](https://pmc.ncbi.nlm.nih.gov/articles/PMC8250995/)
- [Blue light controlled study (PubMed 34627122)](https://pubmed.ncbi.nlm.nih.gov/34627122/)
- [Almeida et al. 2023 — FoMO and pre-sleep arousal (*Scand. J. Psych.*)](https://onlinelibrary.wiley.com/doi/full/10.1111/sjop.12880)
- [Active nudging for digital wellbeing (PMC12310694, 2025)](https://pmc.ncbi.nlm.nih.gov/articles/PMC12310694/)
- [Digital wellbeing app design review (PMC9066336, JMIR 2022)](https://pmc.ncbi.nlm.nih.gov/articles/PMC9066336/)
- [Gradual change procedures in behavior analysis (PMC10050503)](https://pmc.ncbi.nlm.nih.gov/articles/PMC10050503/)
- [Digital behavior change interventions (PMC11161714, 2024)](https://pmc.ncbi.nlm.nih.gov/articles/PMC11161714/)
- [Digital detox and desire-regeneration (*Eur. J. Marketing* 2024)](https://www.emerald.com/ejm/article/doi/10.1108/EJM-05-2024-0373/1328103/Digital-detox-and-the-app-blocking-app-abstinence)
- [JMIR mHealth behavior change techniques for sleep (2022)](https://mhealth.jmir.org/2022/4/e33527)
- [MDPI: Mechanisms linking social media and sleep (2024)](https://www.mdpi.com/2076-328X/14/9/794)

### Official Guidelines & Statistics
- [CDC: About Sleep](https://www.cdc.gov/sleep/about/index.html)
- [CDC: Adult Sleep Facts and Stats](https://www.cdc.gov/sleep/data-research/facts-stats/adults-sleep-facts-and-stats.html)
- [Sleep Foundation: 100+ Sleep Statistics (2024)](https://www.sleepfoundation.org/how-sleep-works/sleep-facts-statistics)
- [PMC: Global Insomnia Prevalence — Systematic Review (2024)](https://pmc.ncbi.nlm.nih.gov/articles/PMC12676268/)
- [Harvard Health: Sleep Hygiene](https://www.health.harvard.edu/staying-healthy/sleep-hygiene-simple-practices-for-better-rest)
- [ResMed: 2024 Global Sleep Survey](https://document.resmed.com/documents/global/2024-Sleep-Survey.pdf)
