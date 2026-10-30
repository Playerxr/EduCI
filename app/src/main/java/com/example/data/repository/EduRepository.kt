package com.example.data.repository

import com.example.data.models.Chapter
import com.example.data.models.QuizQuestion
import com.example.data.models.Subject

class EduRepository {

    fun getDefaultSecondCycleSubjects(): List<Subject> {
        return listOf(
            Subject("math", "Mathématiques", "Calculate", 0xFF1976D2, "Fonctions, Limites, Intégrales, Probabilités, Nombres Complexes", isSelected = true),
            Subject("pc", "Physique-Chimie", "Science", 0xFFD32F2F, "Électrocinétique, Cinématique, Acides-Bases, Chimie Organique", isSelected = true),
            Subject("svt", "SVT", "Biotech", 0xFF388E3C, "Génétique humaine, Immunologie, Écosystèmes, Reproduction", isSelected = true),
            Subject("philo", "Philosophie", "Psychology", 0xFF7B1FA2, "La Connaissance, L'Homme et la Société, L'Art et la Morale", isSelected = true),
            Subject("fr", "Français", "MenuBook", 0xFFE64A19, "Dissertation littéraire, Résumé de texte, Commentaire composé", isSelected = true),
            Subject("hg", "Histoire-Géo", "Public", 0xFF00796B, "La CI de 1960 à nos jours, La Mondialisation, Les Blocs de la Guerre Froide", isSelected = true),
            Subject("en", "Anglais", "Translate", 0xFFC2185B, "Reading Comprehension, Essay Writing, Grammar & Vocabulary", isSelected = true),
            Subject("lv2", "LV2 (Allemand/Espagnol)", "Language", 0xFFF57C00, "Compréhension écrite, Expression, Grammaire & Vocabulaire", isSelected = false),
            Subject("eps", "Éducation Physique", "SportsSoccer", 0xFF43A047, "Règlements des épreuves BAC, Santé, Physiologie du sport", isSelected = false)
        )
    }

    fun getSubjects(): List<Subject> = getDefaultSecondCycleSubjects()

    fun getSampleChapters(): List<Chapter> {
        return listOf(
            // ==================== MATHEMATIQUES ====================
            Chapter(
                id = "math_ch1",
                subjectId = "math",
                title = "Chapitre 1 : Limites & Continuité",
                level = "Terminale",
                series = "D",
                summary = "Maîtriser la continuité, le calcul des limites et la méthode TVI pour le BAC.",
                keyPoints = listOf(
                    "📌 Continuité : lim (x → x₀) f(x) = f(x₀)",
                    "💡 Théorème des Valeurs Intermédiaires (TVI) : f(c) = k",
                    "⚡ Les 4 Formes Indéterminées : 0/0, ∞/∞, +∞-∞, 0×∞",
                    "📐 Asymptotes : Verticale (x=a), Horizontale (y=b), Oblique (y=ax+b)"
                ),
                fullContent = """
                    # 📘 Chapitre 1 : Fonctions Numériques & Limites

                    ---

                    ## I. La Continuité d'une Fonction
                    Une fonction f est continue en un point x₀ si la limite de f(x) quand x tend vers x₀ est égale à f(x₀).
                    
                    > **Formule Officielle du BAC :**
                    > f est continue en x₀ ⟺ lim (x → x₀) f(x) = f(x₀)

                    ---

                    ## II. Le TVI (Théorème des Valeurs Intermédiaires)
                    Le TVI permet de prouver l'existence de solutions à une équation f(x) = 0.
                    
                    ### 📋 Rédaction Type BAC CI :
                    1. "La fonction f est continue sur l'intervalle [a, b]."
                    2. "f(a) × f(b) < 0 (les images sont de signes contraires)."
                    3. "D'après le TVI, il existe au moins un réel α ∈ ]a, b[ tel que f(α) = 0."
                    *Si f est strictement monotone, la solution α est unique !*
                """.trimIndent(),
                formulaSheet = "lim (x→0) (sin x / x) = 1  |  TVI: f(a)·f(b) < 0 ⇒ f(α) = 0  |  Asymptote oblique: lim [f(x) - (ax+b)] = 0",
                bacExamTips = listOf(
                    "🎯 Rédigez systématiquement la phrase de continuité avant le TVI.",
                    "📊 Pensez à vérifier les limites aux bornes du domaine de définition."
                ),
                quizQuestions = listOf(
                    QuizQuestion("q1", "Quelle est la limite de sin(x)/x quand x tend vers 0 ?", listOf("0", "1", "+∞", "Indéterminé"), 1, "C'est une limite usuelle de référence."),
                    QuizQuestion("q2", "Quelle condition garantit l'unicité de la solution α avec le TVI ?", listOf("La dérivabilité", "La stricte monotonie", "La parité", "La périodicité"), 1, "La stricte monotonie assure la bijection.")
                )
            ),
            Chapter(
                id = "math_ch2",
                subjectId = "math",
                title = "Chapitre 2 : Dérivabilité & Étude de Fonctions",
                level = "Terminale",
                series = "D",
                summary = "Étude du nombre dérivé, tangentes, sens de variation et traçage de la courbe représentative.",
                keyPoints = listOf(
                    "📈 Nombre dérivé : f'(x₀) = lim (h → 0) [f(x₀+h) - f(x₀)] / h",
                    "📐 Équation de la Tangente : y = f'(x₀)(x - x₀) + f(x₀)",
                    "⚡ Signe de f'(x) : f'(x) > 0 ⟹ f est strictement croissante",
                    "📍 Points d'inflexion : f''(x) s'annule en changeant de signe"
                ),
                fullContent = """
                    # 📘 Chapitre 2 : Dérivabilité & Étude de Fonctions

                    ---

                    ## I. Nombre Dérivé et Interprétation Géométrique
                    Le nombre dérivé f'(x₀) représente le coefficient directeur de la tangente à la courbe de f au point d'abscisse x₀.

                    ---

                    ## II. Tableau de Variation & Courbe
                    * Calculer f'(x) et étudier son signe.
                    * Dresser le tableau de variation avec les limites aux bornes.
                    * Placer les asymptotes et les points particuliers avant de tracer la courbe (C_f).
                """.trimIndent(),
                formulaSheet = "y = f'(x₀)(x - x₀) + f(x₀)  |  (u/v)' = (u'v - uv') / v²  |  (e^u)' = u' e^u",
                bacExamTips = listOf(
                    "🎯 Vérifiez que votre courbe respecte parfaitement les asymptotes et les tangentes horizontales (f'(x) = 0)."
                ),
                quizQuestions = listOf(
                    QuizQuestion("q_math2_1", "Quelle est l'équation de la tangente au point x₀ ?", listOf("y = f(x₀)", "y = f'(x₀)(x - x₀) + f(x₀)", "y = f'(x₀)x", "y = f(x₀)(x - x₀)"), 1, "C'est la formule fondamentale de la tangente.")
                )
            ),
            Chapter(
                id = "math_ch3",
                subjectId = "math",
                title = "Chapitre 3 : Nombres Complexes",
                level = "Terminale",
                series = "C",
                summary = "Mastering complex numbers: algebraic, trigonometric and exponential forms, plus geometric applications.",
                keyPoints = listOf(
                    "🔑 Unité imaginaire : i² = -1",
                    "📝 Forme algébrique : z = a + i·b",
                    "📐 Forme trigonométrique : z = r(cos θ + i sin θ) et z = r·e^(iθ)",
                    "📍 Distance AB = |zB - zA|"
                ),
                fullContent = """
                    # 📘 Chapitre 3 : Nombres Complexes

                    ---

                    ## I. Les 3 Formes d'un Complexe
                    1. Algébrique : z = a + ib
                    2. Trigonométrique : z = r(cos θ + i sin θ)
                    3. Exponentielle : z = r e^(iθ)

                    ## II. Équations dans C
                    Pour az² + bz + c = 0 avec Δ < 0, les solutions sont z₁,₂ = (-b ± i√|Δ|) / (2a).
                """.trimIndent(),
                formulaSheet = "i² = -1  |  |z| = √(a²+b²)  |  arg(z1 z2) = arg(z1) + arg(z2)",
                bacExamTips = listOf(
                    "🎯 Pour prouver qu'un triangle ABC est équilatéral, montrez que (zC - zA)/(zB - zA) = e^(i π/3)."
                ),
                quizQuestions = listOf(
                    QuizQuestion("q_math3_1", "Que vaut i^4 ?", listOf("i", "-1", "1", "-i"), 2, "i^4 = (i²)² = (-1)² = 1.")
                )
            ),
            Chapter(
                id = "math_ch4",
                subjectId = "math",
                title = "Chapitre 4 : Fonctions ln et Exponentielle",
                level = "Terminale",
                series = "D",
                summary = "Étude approfondie de la fonction Logarithme Népérien ln(x) et de sa réciproque Exponentielle e^x.",
                keyPoints = listOf(
                    "🔑 Réciproques : ln(e^x) = x  et  e^(ln x) = x (pour x > 0)",
                    "📈 Propriété algébrique : ln(a·b) = ln(a) + ln(b)",
                    "⚡ Croissances comparées : lim (x→+∞) (e^x / x) = +∞  |  lim (x→+∞) (ln x / x) = 0",
                    "📐 Dérivées : (ln u)' = u'/u  et  (e^u)' = u'·e^u"
                ),
                fullContent = """
                    # 📘 Chapitre 4 : Fonctions ln & Exponentielle

                    ---

                    ## I. La Fonction Logarithme Népérien (ln)
                    * Domaine : ]0, +∞[
                    * ln(1) = 0 et ln(e) = 1
                    * Dérivée : (ln x)' = 1/x

                    ---

                    ## II. La Fonction Exponentielle (exp)
                    * Domaine : ]-∞, +∞[, strictement positive !
                    * e^0 = 1, e^1 ≈ 2.718
                    * Dérivée : (e^x)' = e^x
                """.trimIndent(),
                formulaSheet = "ln(a/b) = ln a - ln b  |  e^(a+b) = e^a · e^b  |  lim (x→0) (e^x - 1)/x = 1",
                bacExamTips = listOf("🎯 Au BAC CI, n'oubliez jamais le domaine de définition avant de résoudre une équation comportant du ln(x) !"),
                quizQuestions = listOf(
                    QuizQuestion("q_math4_1", "Que vaut ln(e²) ?", listOf("0", "1", "2", "e"), 2, "ln(e^x) = x, donc ln(e²) = 2.")
                )
            ),
            Chapter(
                id = "math_ch5",
                subjectId = "math",
                title = "Chapitre 5 : Intégrales & Primitives",
                level = "Terminale",
                series = "D",
                summary = "Calcul des primitives, intégration par parties (IPP) et calcul d'aires sous la courbe.",
                keyPoints = listOf(
                    "∫ a à b f(x) dx = F(b) - F(a) où F' = f",
                    "Integration par parties (IPP) : ∫ u v' = [u v] - ∫ u' v",
                    "Calcul d'Aires : Area = ∫ |f(x)| dx en U.A."
                ),
                fullContent = "# 📘 Chapitre 5 : Calcul Intégral\nPrimitives usuelles et méthode de l'intégration par parties.",
                formulaSheet = "∫ x^n dx = (1/(n+1)) x^(n+1)  |  ∫ u'/u dx = ln|u|",
                bacExamTips = listOf("🎯 Retenez la règle ALPES pour choisir u(x) lors d'une IPP (Arctan, Logarithme, Polynôme, Exponentielle, Sinus/Cosinus)."),
                quizQuestions = listOf(
                    QuizQuestion("q_math5_1", "Quelle est la primitive de 1/x sur ]0, +∞[ ?", listOf("x²", "ln(x)", "e^x", "-1/x²"), 1, "(ln x)' = 1/x.")
                )
            ),
            Chapter(
                id = "math_ch6",
                subjectId = "math",
                title = "Chapitre 6 : Suites Numériques & Récurrence",
                level = "Terminale",
                series = "D",
                summary = "Démonstration par récurrence, suites arithmétiques/géométriques, convergence et limite.",
                keyPoints = listOf(
                    "Démonstration par récurrence : Initialisation ➔ Hérédité ➔ Conclusion",
                    "Suites géométriques : U_n = U_0 · q^n  |  Somme = U_0 (1 - q^(n+1)) / (1 - q)"
                ),
                fullContent = "# 📘 Chapitre 6 : Suites Numériques\nReasoning by induction and convergence tests.",
                formulaSheet = "Un = U0 + n·r (Arithmétique)  |  Un = U0 · q^n (Géométrique)",
                bacExamTips = listOf("🎯 Pour l'hérédité, écrivez clairement : 'Supposons P(n) vraie, montrons P(n+1)'."),
                quizQuestions = listOf(
                    QuizQuestion("q_math6_1", "Si q = 0.5, quelle est la limite de q^n quand n tend vers +∞ ?", listOf("0", "1", "+∞", "0.5"), 0, "Si |q| < 1, lim q^n = 0.")
                )
            ),
            Chapter(
                id = "math_ch7",
                subjectId = "math",
                title = "Chapitre 7 : Dénombrement & Probabilités",
                level = "Terminale",
                series = "D",
                summary = "P-listes, arrangements C_n^p, tirages simultanés ou successifs, probabilités conditionnelles.",
                keyPoints = listOf(
                    "Tirage simultané ➔ Combinaisons C_n^p",
                    "Tirage successif sans remise ➔ Arrangements A_n^p",
                    "Probabilité conditionnelle : P_B(A) = P(A ∩ B) / P(B)"
                ),
                fullContent = "# 📘 Chapitre 7 : Probabilités & Dénombrement\nComprendre les arbres de probabilité et les lois de probabilité.",
                formulaSheet = "P(A ∪ B) = P(A) + P(B) - P(A ∩ B)  |  C_n^p = n! / [p!(n-p)!]",
                bacExamTips = listOf("🎯 Vérifiez toujours que la somme de vos probabilités est égale à 1 !"),
                quizQuestions = listOf(
                    QuizQuestion("q_math7_1", "Quel outil utilise-t-on pour des tirages simultanés ?", listOf("Arrangements A_n^p", "Combinaisons C_n^p", "Permutations n!", "P-listes n^p"), 1, "Simultané = sans ordre = Combinaison C_n^p.")
                )
            ),
            Chapter(
                id = "math_ch8",
                subjectId = "math",
                title = "Chapitre 8 : Géométrie dans l'Espace & Barycentre",
                level = "Terminale",
                series = "C",
                summary = "Repères de l'espace, produit vectoriel, équations cartésiennes de plans et barycentres.",
                keyPoints = listOf(
                    "Produit scalaire u · v = x x' + y y' + z z'",
                    "Barycentre G de (A,a), (B,b) : a GA + b GB = 0"
                ),
                fullContent = "# 📘 Chapitre 8 : Géométrie dans l'Espace\nVecteurs, plans et représentations paramétriques de droites.",
                formulaSheet = "Equation de plan: ax + by + cz + d = 0  |  Vecteur normal n(a,b,c)",
                bacExamTips = listOf("🎯 Le vecteur normal n(a,b,c) donne directement les coefficients de l'équation du plan !"),
                quizQuestions = listOf(
                    QuizQuestion("q_math8_1", "Si a+b = 0, le barycentre de (A,a) et (B,b) existe-t-il ?", listOf("Oui toujours", "Non il n'existe pas", "Uniquement si A=B", "Dépend du repère"), 1, "Le barycentre existe ssi la somme des masses est non nulle.")
                )
            ),

            // ==================== PHYSIQUE-CHIMIE ====================
            Chapter(
                id = "pc_ch1",
                subjectId = "pc",
                title = "Chapitre 1 : Cinématique & Lois de Newton",
                level = "Terminale",
                series = "D",
                summary = "Étude des mouvements, vitesse, accélération, équations horaires et chute libre de projectiles.",
                keyPoints = listOf(
                    "🏎️ Vecteur vitesse : v(t) = d(OM)/dt",
                    "🚀 Vecteur accélération : a(t) = dv/dt",
                    "⚖️ 2ème Loi de Newton : Σ F_ext = m · a",
                    "🎯 Chute Libre : Soumis uniquement au poids P = m·g ⟹ a = g"
                ),
                fullContent = """
                    # 📘 Chapitre 1 : Cinématique & Lois de Newton

                    ---

                    ## I. Description du Mouvement
                    * **Position OM(t)**
                    * **Vitesse v(t) = dOM/dt**
                    * **Accélération a(t) = dv/dt**

                    ---

                    ## II. La 2ème Loi de Newton
                    Dans un référentiel galiléen : Σ F = m · a.
                """.trimIndent(),
                formulaSheet = "v(t) = a·t + v₀  |  x(t) = ½ a·t² + v₀·t + x₀  |  Σ F = m·a",
                bacExamTips = listOf("🎯 Écrivez toujours : 'Système d'étude = ...' et 'Référentiel terrestre supposé galiléen'."),
                quizQuestions = listOf(
                    QuizQuestion("q_pc1", "Quelle est l'accélération d'un corps en chute libre sans frottement ?", listOf("a = 0", "a = g", "a = m·g", "a = v²/R"), 1, "ΣF = P = m·g => m·a = m·g => a = g.")
                )
            ),
            Chapter(
                id = "pc_ch2",
                subjectId = "pc",
                title = "Chapitre 2 : Mouvement dans un Champ Électrique",
                level = "Terminale",
                series = "D",
                summary = "Mouvement d'une particule chargée (électron, proton) entre les plaques d'un condensateur plan.",
                keyPoints = listOf(
                    "⚡ Force électrique : F = q · E",
                    "📐 Champ électrique : E = U / d (entre deux plaques distantes de d)",
                    "📈 Déviation électrique Y sur l'écran"
                ),
                fullContent = "# 📘 Chapitre 2 : Particules Chargées dans un Champ E\nÉtude des trajectoires paraboliques dans un condensateur.",
                formulaSheet = "F = q·E  |  a = (q·E)/m  |  E = U/d",
                bacExamTips = listOf("🎯 Faites attention au signe de la charge q (ex: q = -e pour un électron) !"),
                quizQuestions = listOf(
                    QuizQuestion("q_pc2", "Quelle est la valeur du champ E entre deux plaques distantes de d sous une tension U ?", listOf("U · d", "U / d", "d / U", "U² / d"), 1, "E = U / d en Volts par mètre (V/m).")
                )
            ),
            Chapter(
                id = "pc_ch3",
                subjectId = "pc",
                title = "Chapitre 3 : Circuits Électriques RLC",
                level = "Terminale",
                series = "D",
                summary = "Charge et décharge d'un condensateur, dipôle RC, RL et oscillations libres RLC.",
                keyPoints = listOf(
                    "⚡ Tension condensateur : u_C = q / C",
                    "⏱️ Constante de temps RC : τ = R · C",
                    "🌊 Période propre RLC : T₀ = 2π √(L·C)"
                ),
                fullContent = "# 📘 Chapitre 3 : Circuits RLC Oscillants\nÉquations différentielles et phénomène de résonance.",
                formulaSheet = "τ = R·C  |  T₀ = 2π √(L C)  |  E_em = ½ C u_C² + ½ L i²",
                bacExamTips = listOf("🎯 Au BAC, sachez retrouver l'équation différentielle en appliquant la loi des mailles."),
                quizQuestions = listOf(
                    QuizQuestion("q_pc3", "Quelle est la période propre d'un circuit LC idéal ?", listOf("T₀ = R C", "T₀ = 2π √(L C)", "T₀ = L / R", "T₀ = 1 / (LC)"), 1, "Formule de Thomson : T₀ = 2π √(L C).")
                )
            ),
            Chapter(
                id = "pc_ch4",
                subjectId = "pc",
                title = "Chapitre 4 : Acides & Bases en Solution Aqueuse",
                level = "Terminale",
                series = "D",
                summary = "pH, produit ionique de l'eau Ke, acides forts/faibles et constantes d'acidité Ka/pKa.",
                keyPoints = listOf(
                    "🧪 pH = -log[H3O+]",
                    "💧 Ke = [H3O+][OH-] = 10⁻¹⁴ à 25°C",
                    "📈 pH = pKa + log([Base]/[Acide])"
                ),
                fullContent = "# 📘 Chapitre 4 : Acides et Bases\nComprendre les couples acido-basiques et la prédominance des espèces.",
                formulaSheet = "pH = -log[H3O+]  |  pH = pKa + log([A⁻]/[AH])",
                bacExamTips = listOf("🎯 Si pH > pKa, la forme basique [A⁻] prédomine !"),
                quizQuestions = listOf(
                    QuizQuestion("q_pc4", "Quel est le pH d'une solution neutre à 25°C ?", listOf("0", "7", "14", "1"), 1, "À 25°C, solution neutre ⟹ pH = 7.")
                )
            ),
            Chapter(
                id = "pc_ch5",
                subjectId = "pc",
                title = "Chapitre 5 : Titrage Acido-Basique",
                level = "Terminale",
                series = "D",
                summary = "Dosage pH-métrique, repérage du point d'équivalence E et calcul de concentrations inconnues.",
                keyPoints = listOf(
                    "🎯 Équivalence : Ca · Va = Cb · Vbe",
                    "📊 Méthode des tangentes parallèles pour trouver E",
                    "💧 Demi-équivalence : pH = pKa"
                ),
                fullContent = "# 📘 Chapitre 5 : Titrage Acido-Basique\nMéthodes expérimentales de dosage au BAC.",
                formulaSheet = "Ca · Va = Cb · Vbe  |  À la demi-équivalence: pH = pKa",
                bacExamTips = listOf("🎯 À la demi-équivalence (Vb = Vbe/2), le pH de la solution est égal au pKa du couple !"),
                quizQuestions = listOf(
                    QuizQuestion("q_pc5", "Quelle relation régit l'équivalence d'un dosage acide fort par une base forte ?", listOf("Ca Va = Cb Vbe", "Ca / Va = Cb / Vbe", "pH = 7", "pKa = 14"), 0, "Ca Va = Cb Vbe exprime la conservation de la quantité de matière.")
                )
            ),
            Chapter(
                id = "pc_ch6",
                subjectId = "pc",
                title = "Chapitre 6 : Chimie Organique - Estérification & Saponification",
                level = "Terminale",
                series = "D",
                summary = "Réactions entre acides carboxyliques et alcools, caractéristiques de l'estérification et savonnerie.",
                keyPoints = listOf(
                    "🍋 Acide + Alcool ⇌ Ester + Eau",
                    "⚡ Caractéristiques : Lente, limitée, athermalique",
                    "🧼 Saponification : Ester + Soude (NaOH) ➔ Savon + Glycérol (totale et rapide !)"
                ),
                fullContent = "# 📘 Chapitre 6 : Estérification et Saponification\nMécanismes de la chimie organique industrielle.",
                formulaSheet = "R-COOH + R'-OH ⇌ R-COO-R' + H2O",
                bacExamTips = listOf("🎯 La saponification est une réaction TOTALE contrairement à l'estérification qui est un équilibre limité !"),
                quizQuestions = listOf(
                    QuizQuestion("q_pc6", "Quelles sont les caractéristiques de l'estérification directe ?", listOf("Rapide et totale", "Lente, limitée et athermalique", "Exothermique et violente", "Impossible sans lumière"), 1, "L'estérification directe est lente, athermique et limitée par son hydrolyse inverse.")
                )
            ),
            Chapter(
                id = "pc_ch7",
                subjectId = "pc",
                title = "Chapitre 7 : Optique Géométrique - Lentilles Minces",
                level = "Terminale",
                series = "D",
                summary = "Formule de conjugaison de Descartes, grandissement γ, vergence C et construction d'images.",
                keyPoints = listOf(
                    "👓 Vergence C = 1 / f' (en Dioptries δ)",
                    "📐 Formule de Descartes : 1/OA' - 1/OA = 1/f'",
                    "🔍 Grandissement γ = A'B' / AB = OA' / OA"
                ),
                fullContent = "# 📘 Chapitre 7 : Optique Géométrique\nLentilles convergentes, lunette astronomique et microscope.",
                formulaSheet = "1/OA' - 1/OA = 1/f'  |  C = 1/f'  |  γ = OA'/OA",
                bacExamTips = listOf("🎯 N'oubliez pas la convention de signes (mesures algébriques sur l'axe optique) !"),
                quizQuestions = listOf(
                    QuizQuestion("q_pc7", "Quelle est l'unité de la vergence d'une lentille ?", listOf("Mètre", "Dioptrie (δ)", "Joule", "Hertz"), 1, "La vergence s'exprime en dioptries (δ = m⁻¹).")
                )
            ),
            Chapter(
                id = "pc_ch8",
                subjectId = "pc",
                title = "Chapitre 8 : Physique Nucléaire & Radioactivité",
                level = "Terminale",
                series = "D",
                summary = "Radioactivité α, β-, β+, γ, loi de décroissance radioactive, demi-vie T et bilan d'énergie ΔE = Δm · c².",
                keyPoints = listOf(
                    "⚛️ Loi de Soddy : Conservation du nombre de masse A et de charge Z",
                    "⏱️ Demi-vie t1/2 : N(t1/2) = N0 / 2",
                    "💥 Énergie de liaison : ΔE = Δm · c²"
                ),
                fullContent = "# 📘 Chapitre 8 : Physique Nucléaire\nNoyaux, isotopes, réactions de fission et de fusion nucléaire.",
                formulaSheet = "N(t) = N0 e^(-λ t)  |  t1/2 = (ln 2) / λ  |  E = m c²",
                bacExamTips = listOf("🎯 Exprimez bien le défaut de masse Δm en kg ou en u (1 u = 931.5 MeV/c²)."),
                quizQuestions = listOf(
                    QuizQuestion("q_pc8", "Quelle est la relation entre la demi-vie t1/2 et la constante radioactive λ ?", listOf("t1/2 = λ / ln 2", "t1/2 = (ln 2) / λ", "t1/2 = λ · ln 2", "t1/2 = 1 / λ"), 1, "t1/2 = (ln 2) / λ.")
                )
            ),

            // ==================== SVT ====================
            Chapter(
                id = "svt_ch1",
                subjectId = "svt",
                title = "Chapitre 1 : Immunologie - L'Immunité Innée",
                level = "Terminale",
                series = "D",
                summary = "La réaction inflammatoire aiguë, les 4 signes cardinaux et le mécanisme de la phagocytose.",
                keyPoints = listOf(
                    "🛡️ Immunité Innée : Défense immédiate, non spécifique et sans mémoire",
                    "🔥 4 Signes Inflammatoires : Rougeur, Chaleur, Gonflement (Tuméfaction), Douleur",
                    "🦠 Phagocytose : Adhésion ➔ Ingestion ➔ Digestion ➔ Rejet des déchets"
                ),
                fullContent = "# 📘 Chapitre 1 : Immunité Innée\nLa première ligne de défense de l'organisme humain.",
                formulaSheet = "Phagocytose ➔ Macrophages, Neutrophiles & Histamine",
                bacExamTips = listOf("🎯 Citez toujours les 4 signes cardinaux de la réaction inflammatoire !"),
                quizQuestions = listOf(
                    QuizQuestion("q_svt1", "Lequel n'est PAS un signe de la réaction inflammatoire ?", listOf("Rougeur", "Douleur", "Frisson", "Chaleur"), 2, "Les 4 signes sont Rougeur, Chaleur, Gonflement, Douleur.")
                )
            ),
            Chapter(
                id = "svt_ch2",
                subjectId = "svt",
                title = "Chapitre 2 : Immunologie - L'Immunité Adaptative & SIDA",
                level = "Terminale",
                series = "D",
                summary = "Les voies humorale (LB) et cellulaire (LT8), le rôle pivot des LT4 et le virus du VIH/SIDA.",
                keyPoints = listOf(
                    "🩸 Voie Humorale : LB ➔ Plasmocytes ➔ Sécrétion d'Anticorps",
                    "🗡️ Voie Cellulaire : LT8 ➔ LT Cytotoxiques (LTC) ➔ Destruction des cellules infectées",
                    "👑 Chef d'Orchestre : LT4 et sécrétion d'interleukines"
                ),
                fullContent = "# 📘 Chapitre 2 : Immunité Adaptative & VIH\nComment le VIH cible les LT4 et détruit l'immunité.",
                formulaSheet = "LB ➔ Plasmocytes ➔ Anticorps | LT8 ➔ LTC | LT4 ➔ Interleukines",
                bacExamTips = listOf("🎯 Accompagnez toujours votre devoir de synthèse en SVT d'un schéma bilan fléché !"),
                quizQuestions = listOf(
                    QuizQuestion("q_svt2", "Quelles cellules fabriquent et sécrètent les anticorps ?", listOf("Macrophages", "Plasmocytes", "LT4", "Neutrophiles"), 1, "Les LB stimulés se différencient en plasmocytes pour produire des anticorps.")
                )
            ),
            Chapter(
                id = "svt_ch3",
                subjectId = "svt",
                title = "Chapitre 3 : Génétique Humaine & Hérédité",
                level = "Terminale",
                series = "D",
                summary = "Analyse des arbres généalogiques (pedigrees), transmission des maladies autosomiques ou liées au sexe (X/Y).",
                keyPoints = listOf(
                    "🧬 Allèle dominant vs récessif",
                    "♂️♀️ Maladie liée à X : Hommes hémizygotes plus fréquemment touchés",
                    "📊 Calcul du risque génétique de transmission"
                ),
                fullContent = "# 📘 Chapitre 3 : Génétique Humaine\nMéthodologie d'analyse des arbres généalogiques au BAC.",
                formulaSheet = "Dominant ➔ Présent à toutes les générations | Récessif ➔ Saut de génération",
                bacExamTips = listOf("🎯 Pour prouver qu'un allèle est récessif, cherchez deux parents sains ayant un enfant malade !"),
                quizQuestions = listOf(
                    QuizQuestion("q_svt3", "Comment prouver qu'un allèle morbide est récessif sur un pedigree ?", listOf("Toutes les filles sont malades", "Deux parents sains ont un enfant atteint", "La maladie ne touche que les hommes", "Deux parents malades ont un enfant sain"), 1, "Si des parents sains ont un enfant malade, les parents sont porteurs sains (hétérozygotes).")
                )
            ),
            Chapter(
                id = "svt_ch4",
                subjectId = "svt",
                title = "Chapitre 4 : La Reproduction Chez l'Homme",
                level = "Terminale",
                series = "D",
                summary = "Spermatogenèse, folliculogenèse, cycles ovarien et utérin, rétrocontrôles de l'axe hypothalamo-hypophysaire.",
                keyPoints = listOf(
                    "🧠 GnRH (Hypothalamus) ➔ LH et FSH (Hypophyse)",
                    "♂️ Testostérone et rétrocontrôle négatif",
                    "♀️ Pic de LH au 14ème jour ➔ Déclenchement de l'Ovulation"
                ),
                fullContent = "# 📘 Chapitre 4 : Régulation de la Reproduction\nHorloges biologiques et cycles hormonaux chez l'Homme.",
                formulaSheet = "GnRH ➔ LH/FSH ➔ Œstrogènes/Progestérone ➔ Rétrocontrôle",
                bacExamTips = listOf("🎯 Le pic d'œstrogènes avant le 14ème jour exerce un RÉTROCONTRÔLE POSITIF qui provoque le pic de LH !"),
                quizQuestions = listOf(
                    QuizQuestion("q_svt4", "Quelle hormone déclenche l'ovulation au 14ème jour du cycle féminin ?", listOf("La Progestérone", "Le pic de LH", "La Testostérone", "L'HCG"), 1, "Le décharge ovulante de LH provoque la rupture du follicule mûr.")
                )
            ),
            Chapter(
                id = "svt_ch5",
                subjectId = "svt",
                title = "Chapitre 5 : Le Système Nerveux & Le Réflexe Myotatique",
                level = "Terminale",
                series = "D",
                summary = "Anatomie de l'arc réflexe, fuseau neuromusculaire, potentiel d'action et transmission synaptique.",
                keyPoints = listOf(
                    "⚡ Potentiel d'action (PA) : Loi du tout ou rien",
                    "🧠 Synapse : Libération de neurotransmetteurs (Acétylcholine)",
                    "🦴 Réflexe myotatique : Contraction involontaire d'un muscle étiré"
                ),
                fullContent = "# 📘 Chapitre 5 : Le Système Nerveux\nCommunication nerveuse, codage en fréquence de PA et rôle des synapses.",
                formulaSheet = "Stimulus ➔ Récepteur ➔ Voie Afférente ➔ Centre Nerveux ➔ Voie Efférente ➔ Effecteur",
                bacExamTips = listOf("🎯 Le codage du message nerveux le long de l'axone se fait en FRÉQUENCE de potentiels d'action !"),
                quizQuestions = listOf(
                    QuizQuestion("q_svt5", "Comment le message nerveux est-il codé le long d'une fibre nerveuse ?", listOf("En amplitude de PA", "En fréquence de PA", "En vitesse seulement", "En couleur"), 1, "L'amplitude d'un PA est constante : le message est codé en fréquence.")
                )
            ),
            Chapter(
                id = "svt_ch6",
                subjectId = "svt",
                title = "Chapitre 6 : La Synthèse des Protéines",
                level = "Terminale",
                series = "D",
                summary = "Du gène à la protéine : Transcription de l'ADN en ARNm dans le noyau et Traduction dans le cytoplasme.",
                keyPoints = listOf(
                    "🧬 Transcription : ADN ➔ ARNm par l'ARN polymérase (Noyau)",
                    "⚙️ Traduction : ARNm ➔ Chaîne de Protéines par les Ribosomes (Cytoplasme)",
                    "📖 Code Génétique : Universel, redondant (dégénéré) et non chevauchant"
                ),
                fullContent = "# 📘 Chapitre 6 : Synthèse des Protéines\nLe dogme central de la biologie moléculaire.",
                formulaSheet = "Codon Initiateur = AUG (Méthionine)  |  Codons Stop = UAA, UAG, UGA",
                bacExamTips = listOf("🎯 Rappelez-vous que dans l'ARN, la base Thymine (T) est remplacée par l'Uracile (U) !"),
                quizQuestions = listOf(
                    QuizQuestion("q_svt6", "Où se déroule la traduction de l'ARNm en protéine ?", listOf("Dans le noyau", "Dans le cytoplasme (sur les ribosomes)", "Dans la membrane", "Dans la vacuole"), 1, "La traduction s'effectue dans le cytoplasme au niveau des ribosomes.")
                )
            ),
            Chapter(
                id = "svt_ch7",
                subjectId = "svt",
                title = "Chapitre 7 : Écosystèmes & Pédologie en Côte d'Ivoire",
                level = "Terminale",
                series = "D",
                summary = "Structure des sols ivoiriens, latéritisation, réseaux trophiques et préservation de la forêt guinéenne.",
                keyPoints = listOf(
                    "🌱 Pédologie : Sol ferrallitique (latérite) typique des zones tropicales",
                    "🌳 Biodiversité : Forêt dense humide vs Savane arborée",
                    "♻️ Recyclage de la matière organique et compostage"
                ),
                fullContent = "# 📘 Chapitre 7 : Écosystèmes & Sols de Côte d'Ivoire\nProtection des ressources naturelles et développement durable.",
                formulaSheet = "Chaine alimentaire : Producteurs Primaires ➔ Consommateurs ➔ Décomposeurs",
                bacExamTips = listOf("🎯 Citez des exemples concrets du patrimoine forestier ivoirien (ex: Parc National de Taï)."),
                quizQuestions = listOf(
                    QuizQuestion("q_svt7", "Quel type de sol prédomine en zone forestière humide en Côte d'Ivoire ?", listOf("Sol podzolique", "Sol ferrallitique", "Sol volcanique", "Sol calcaire"), 1, "Le sol ferrallitique riche en fer et aluminium est caractéristique du climat équatorial/tropical humidifié.")
                )
            ),
            Chapter(
                id = "svt_ch8",
                subjectId = "svt",
                title = "Chapitre 8 : Géologie - Tectonique des Plaques",
                level = "Terminale",
                series = "D",
                summary = "Divergence aux dorsales, convergence et subduction, magmatisme et métamorphisme des roches.",
                keyPoints = listOf(
                    "🌍 Tectonique : Lithosphère découpée en plaques rigides",
                    "🌋 Subduction : Enfoncement d'une plaque océanique sous une plaque continentale",
                    "🪨 Métamorphisme : Transformation à l'état solide sous l'effet de la pression et température"
                ),
                fullContent = "# 📘 Chapitre 8 : Tectonique des Plaques & Roches\nLa dynamique interne de la Terre et la géologie régionale.",
                formulaSheet = "Dorsale (Accrétion) vs Fosse (Subduction)",
                bacExamTips = listOf("🎯 Les séismes profonds le long du plan de Wadati-Benioff caractérisent les zones de subduction !"),
                quizQuestions = listOf(
                    QuizQuestion("q_svt8", "Où se crée la nouvelle croûte océanique ?", listOf("Dans les fosses de subduction", "Au niveau des dorsales océaniques", "Dans les chaînes de montagnes", "Dans le noyau externe"), 1, "L'accrétion océanique se produit au niveau des dorsales.")
                )
            ),

            // ==================== PHILOSOPHIE ====================
            Chapter(
                id = "philo_ch1",
                subjectId = "philo",
                title = "Chapitre 1 : Introduction à la Philosophie",
                level = "Terminale",
                series = "A",
                summary = "Origine de la philosophie, l'étonnement socratique, le rejet du mythe et du préjugé.",
                keyPoints = listOf(
                    "🤔 L'Étonnement (Platon/Aristote) : Point de départ de la réflexion",
                    "💡 Doute méthodique (Descartes) : Libérer l'esprit des certitudes dogmatiques",
                    "📜 'Connais-toi toi-même' (Socrate)"
                ),
                fullContent = "# 📘 Chapitre 1 : L'Étonnement Philosophique\nPasser du Mythos au Logos.",
                formulaSheet = "Socrate : 'Tout ce que je sais, c'est que je ne sais rien.'",
                bacExamTips = listOf("🎯 Montrez que la philosophie n'est pas un savoir accumulé mais un exercice critique de la raison !"),
                quizQuestions = listOf(
                    QuizQuestion("q_p1", "Qui est l'auteur de la célèbre formule 'Connais-toi toi-même' ?", listOf("Aristote", "Socrate", "Nietzsche", "Kant"), 1, "Socrate a fait du principe pythique le cœur de sa philosophie réflexive.")
                )
            ),
            Chapter(
                id = "philo_ch2",
                subjectId = "philo",
                title = "Chapitre 2 : La Conscience & L'Inconscient",
                level = "Terminale",
                series = "A",
                summary = "Le Cogito cartésien, la psychanalyse freudienne et les critiques de l'inconscient (Alain, Sartre).",
                keyPoints = listOf(
                    "💭 Cogito : 'Je pense donc je suis' (Descartes)",
                    "🛋️ L'Inconscient (Freud) : Ça, Moi, Surmoi et le refoulement",
                    "⚠️ Critique de la mauvaise foi (Sartre) : L'Homme est condamné à être libre"
                ),
                fullContent = "# 📘 Chapitre 2 : La Conscience & L'Inconscient\nSommes-nous maîtres de notre esprit ?",
                formulaSheet = "Descartes (Souveraineté du Moi) vs Freud (L'Inconscient) vs Sartre (Mauvaise foi)",
                bacExamTips = listOf("🎯 Ne réduisez pas l'Homme à l'inconscient : montrez aussi la responsabilité morale et la conscience !"),
                quizQuestions = listOf(
                    QuizQuestion("q_p2", "Qui a inventé la psychanalyse et théorisé l'inconscient ?", listOf("René Descartes", "Sigmund Freud", "Jean-Paul Sartre", "Blaise Pascal"), 1, "Sigmund Freud est le père de la psychanalyse.")
                )
            ),
            Chapter(
                id = "philo_ch3",
                subjectId = "philo",
                title = "Chapitre 3 : Le Travail & La Technique",
                level = "Terminale",
                series = "A",
                summary = "Le travail comme libération humanisante (Hegel) ou comme source d'aliénation (Marx).",
                keyPoints = listOf(
                    "🔨 Hegel : Le travail façonne le monde et libère la conscience",
                    "⛓️ Karl Marx : L'aliénation de l'ouvrier dépossédé du fruit de son labeur",
                    "⚙️ La technique : Maîtrise du monde ou risque de technocratie ?"
                ),
                fullContent = "# 📘 Chapitre 3 : Le Travail & La Technique\nLa double nature du travail humain.",
                formulaSheet = "Hegel (Humanisation) vs Marx (Aliénation) vs Descartes (Maîtres et possesseurs)",
                bacExamTips = listOf("🎯 Définissez le mot travail (tripalium) dès l'introduction pour soulever le paradoxe !"),
                quizQuestions = listOf(
                    QuizQuestion("q_p3", "Quel philosophe théorise l'aliénation du travailleur dans le capitalisme ?", listOf("Hegel", "Karl Marx", "Descartes", "Kant"), 1, "Karl Marx analyse l'aliénation dans 'Le Capital'.")
                )
            ),
            Chapter(
                id = "philo_ch4",
                subjectId = "philo",
                title = "Chapitre 4 : L'État, Le Droit & La Justice",
                level = "Terminale",
                series = "A",
                summary = "Le contrat social (Rousseau, Hobbes), l'état de nature, le rôle des lois et la justice distributive.",
                keyPoints = listOf(
                    "📜 Rousseau : Le Contrat Social et la Volonté Générale",
                    "🐺 Hobbes : 'L'homme est un loup pour l'homme' à l'état de nature",
                    "⚖️ Spinoza : La fin de l'État est en réalité la liberté"
                ),
                fullContent = "# 📘 Chapitre 4 : L'État & La Justice\nPourquoi obéir aux lois de la société ?",
                formulaSheet = "État de Nature ➔ Contrat Social ➔ État de Droit",
                bacExamTips = listOf("🎯 Distinguez bien le Droit Positif (lois écrites) du Droit Naturel (justice idéale) !"),
                quizQuestions = listOf(
                    QuizQuestion("q_p4", "Selon Jean-Jacques Rousseau, sur quoi repose l'État légitime ?", listOf("La force brute du prince", "Le Contrat Social et la Volonté Générale", "La tradition religieuse", "L'argent"), 1, "Rousseau fonde la démocratie sur la souveraineté du peuple par le contrat social.")
                )
            ),
            Chapter(
                id = "philo_ch5",
                subjectId = "philo",
                title = "Chapitre 5 : L'Art, La Culture & L'Esthétique",
                level = "Terminale",
                series = "A",
                summary = "Définition du beau (Kant), l'œuvre d'art vs le produit technique, la fonction sociale de l'art.",
                keyPoints = listOf(
                    "🎨 Kant : 'Le beau est ce qui plaît universellement sans concept'",
                    "🎭 L'Art : Expression désintéressée de la liberté humaine",
                    "🏛️ La culture : Dépassement de la nature brute"
                ),
                fullContent = "# 📘 Chapitre 5 : L'Art & L'Esthétique\nLe sens de la création artistique.",
                formulaSheet = "Beau (Désintéressé) vs Utile (Technique)",
                bacExamTips = listOf("🎯 L'œuvre d'art n'a pas d'utilité pratique immédiate : sa valeur est esthétique et poétique !"),
                quizQuestions = listOf(
                    QuizQuestion("q_p5", "Selon Emmanuel Kant, quelle est la caractéristique du jugement du Beau ?", listOf("Il est intéressé par le profit", "Il plaît universellement sans concept", "Il dépend uniquement du prix", "Il est scientifique"), 1, "Pour Kant, le beau suscite une satisfaction désintéressée.")
                )
            ),
            Chapter(
                id = "philo_ch6",
                subjectId = "philo",
                title = "Chapitre 6 : La Vérité Scientifique & Le Progrès",
                level = "Terminale",
                series = "A",
                summary = "Épistémologie : vérité formelle vs vérité matérielle, la réfutabilité de Popper et l'esprit scientifique (Bachelard).",
                keyPoints = listOf(
                    "🧪 Bachelard : 'La science s'édifie contre le sens commun'",
                    "🎯 Karl Popper : Une théorie scientifique doit être réfutable (falsifiable)",
                    "⚖️ Raisonnement déductif vs inductif"
                ),
                fullContent = "# 📘 Chapitre 6 : La Vérité Scientifique\nComment se construisent les connaissances vérifiables.",
                formulaSheet = "Bachelard (Obstacle épistémologique) | Popper (Réfutabilité)",
                bacExamTips = listOf("🎯 Une théorie qu'on ne peut jamais soumettre à l'expérience n'est pas scientifique (Popper) !"),
                quizQuestions = listOf(
                    QuizQuestion("q_p6", "Selon Karl Popper, quel est le critère d'une véritable théorie scientifique ?", listOf("Sa certitude absolue", "Sa réfutabilité (ou falsifiabilité)", "Sa popularité", "Son ancienneté"), 1, "Une théorie doit pouvoir être testée et éventuellement contredite par l'expérience.")
                )
            ),
            Chapter(
                id = "philo_ch7",
                subjectId = "philo",
                title = "Chapitre 7 : Méthodologie de la Dissertation Philosophique",
                level = "Terminale",
                series = "A",
                summary = "Analyse du sujet, problématisation, plan dialectique (Thèse / Antithèse / Synthèse) et rédaction.",
                keyPoints = listOf(
                    "1. Analyse des termes du sujet",
                    "2. Problématique sous forme de paradoxe",
                    "3. Plan rédigé avec auteurs et arguments précis"
                ),
                fullContent = "# 📘 Chapitre 7 : Méthode de la Dissertation Philosophique\nLe guide pas à pas du BAC de Philosophie.",
                formulaSheet = "Intro (Amorce ➔ Sujet ➔ Problématique ➔ Plan) | Dev (I. Thèse, II. Antithèse, III. Synthèse)",
                bacExamTips = listOf("🎯 N'oubliez jamais d'illustrer chaque argument par un auteur philosophique précis !"),
                quizQuestions = listOf(
                    QuizQuestion("q_p7", "Quelle partie de l'introduction contient la question centrale posée par le sujet ?", listOf("L'amorce", "La problématique", "L'annonce du plan", "La conclusion"), 1, "La problématique met en évidence la contradiction ou le problème du sujet.")
                )
            ),
            Chapter(
                id = "philo_ch8",
                subjectId = "philo",
                title = "Chapitre 8 : Méthodologie du Commentaire de Texte",
                level = "Terminale",
                series = "A",
                summary = "Dégager le thème, le problème, la thèse de l'auteur, la structure logique et faire l'intérêt philosophique.",
                keyPoints = listOf(
                    "🔍 Thème : De quoi parle le texte ?",
                    "💡 Thèse : Que soutient l'auteur ?",
                    "📊 Mouvements du texte : Découpage logique"
                ),
                fullContent = "# 📘 Chapitre 8 : Méthode du Commentaire de Texte\nExplication linéaire et évaluation critique du texte.",
                formulaSheet = "Thème ➔ Problème ➔ Thèse ➔ Structure ➔ Explication ➔ Intérêt philosophique",
                bacExamTips = listOf("🎯 Ne faites pas de paraphrase : expliquez les concepts clés employés par l'auteur !"),
                quizQuestions = listOf(
                    QuizQuestion("q_p8", "Que représente la 'thèse' d'un auteur dans un texte philosophique ?", listOf("Sa biographie", "La position centrale qu'il défend dans le texte", "Le résumé de la conclusion", "Une citation célèbre"), 1, "La thèse est l'idée majeure que l'auteur veut démontrer.")
                )
            ),

            // ==================== FRANÇAIS ====================
            Chapter(
                id = "fr_ch1",
                subjectId = "fr",
                title = "Chapitre 1 : La Dissertation Littéraire",
                level = "Terminale",
                series = "A",
                summary = "Méthode rigoureuse de la dissertation littéraire au BAC de Français en Côte d'Ivoire.",
                keyPoints = listOf(
                    "📝 Analyse de la citation littéraire",
                    "📚 Citations d'œuvres ivoiriennes et africaines (Bernard Dadié, Ahmadou Kourouma, Camara Laye)",
                    "💡 Organisation du plan dialectique"
                ),
                fullContent = "# 📘 Chapitre 1 : Dissertation Littéraire\nRéussir l'épreuve écrite du BAC de Français.",
                formulaSheet = "Intro ➔ Développement (I. Accord, II. Nuance/Limite, III. Synthèse) ➔ Conclusion",
                bacExamTips = listOf("🎯 Apprenez au moins 3 citations d'auteurs ivoiriens par genre littéraire (Roman, Poésie, Théâtre) !"),
                quizQuestions = listOf(
                    QuizQuestion("q_fr1", "Qui est l'auteur du célèbre roman ivoirien 'Climbié' ?", listOf("Ahmadou Kourouma", "Bernard Dadié", "Jean-Marie Adiaffi", "Camara Laye"), 1, "Bernard Dadié est l'écrivain émérite auteur de 'Climbié'.")
                )
            ),
            Chapter(
                id = "fr_ch2",
                subjectId = "fr",
                title = "Chapitre 2 : La Poésie & La Négritude",
                level = "Terminale",
                series = "A",
                summary = "Le mouvement de la Négritude (Aimé Césaire, Léopold Sédar Senghor, Bernard Dadié) et la poésie engagée.",
                keyPoints = listOf(
                    "🌍 La Négritude : Affirmation de l'identité et des valeurs culturelles noires",
                    "✍️ Formes poétiques : Vers libre, images poétiques, lyrisme et engagement",
                    "📜 Césaire : 'Cahier d'un retour au pays natal'"
                ),
                fullContent = "# 📘 Chapitre 2 : Poésie & Négritude\nLes grandes voix poétiques africaines et antillaises.",
                formulaSheet = "Césaire (Révolte) | Senghor (Conciliation) | Dadié (Fraternité)",
                bacExamTips = listOf("🎯 Mettez en valeur les métaphores et le rythme des poèmes dans vos devoirs !"),
                quizQuestions = listOf(
                    QuizQuestion("q_fr2", "Qui sont les trois pères fondateurs du mouvement de la Négritude ?", listOf("Dadié, Kourouma, Laye", "Césaire, Senghor, Damas", "Hugo, Baudelaire, Verlaine", "Achebe, Soyinka, Beti"), 1, "Aimé Césaire, Léopold Sédar Senghor et Léon-Gontran Damas ont fondé le mouvement à Paris.")
                )
            ),
            Chapter(
                id = "fr_ch3",
                subjectId = "fr",
                title = "Chapitre 3 : Le Roman Africain & Ivoirien",
                level = "Terminale",
                series = "A",
                summary = "Évolution du roman africain : de la contestation coloniale (Camara Laye) à la critique des indépendances (Kourouma).",
                keyPoints = listOf(
                    "📖 Ahmadou Kourouma : 'Les Soleils des Indépendances' et la rupture stylistique",
                    "🌱 Camara Laye : 'L'Enfant Noir' et la nostalgie des traditions",
                    "🎭 Le roman comme miroir de la société africaine"
                ),
                fullContent = "# 📘 Chapitre 3 : Le Roman Africain\nL'histoire du roman du XXe siècle à aujourd'hui.",
                formulaSheet = "Kourouma (Les Soleils des Indépendances) | Laye (L'Enfant Noir) | Dadié (Climbié)",
                bacExamTips = listOf("🎯 Montrez comment Kourouma intègre les structures de la langue malinké dans le français !"),
                quizQuestions = listOf(
                    QuizQuestion("q_fr3", "Quel roman d'Ahmadou Kourouma inaugure une écriture romanesque renouvelée en 1968 ?", listOf("Climbié", "Les Soleils des Indépendances", "L'Aventure Ambiguë", "Ville Cruelle"), 1, "'Les Soleils des Indépendances' est un chef-d'œuvre incontournable.")
                )
            ),
            Chapter(
                id = "fr_ch4",
                subjectId = "fr",
                title = "Chapitre 4 : Le Théâtre & L'Engagement",
                level = "Terminale",
                series = "A",
                summary = "Le théâtre historique et politique en Afrique : Bernard Dadié ('Béatrice du Congo'), Jean-Marie Adiaffi.",
                keyPoints = listOf(
                    "🎭 Fonction de la comédie et de la tragédie",
                    "📜 Le théâtre historique : Éveiller les consciences patriotiques",
                    "💡 Mise en scène et didascalies"
                ),
                fullContent = "# 📘 Chapitre 4 : Le Théâtre Africain\nLe théâtre comme tribune de contestation et de sensibilisation.",
                formulaSheet = "Castigat ridendo mores (Châtier les mœurs en riant)",
                bacExamTips = listOf("🎯 Dans un sujet sur le théâtre, ne parlez pas seulement du texte, pensez aussi à la représentation scénique !"),
                quizQuestions = listOf(
                    QuizQuestion("q_fr4", "Quelle est la pièce de théâtre historique de Bernard Dadié dénonçant la colonisation au Congo ?", listOf("Béatrice du Congo", "Monsieur Thôgô-gnini", "Trois Mégots", "Les Voix dans le vent"), 0, "'Béatrice du Congo' met en scène la résistance de Kimpa Vita.")
                )
            ),
            Chapter(
                id = "fr_ch5",
                subjectId = "fr",
                title = "Chapitre 5 : Résumé de Texte & Discussion",
                level = "Terminale",
                series = "A",
                summary = "Techniques de condensation au 1/4 du texte d'origine et méthode de la discussion.",
                keyPoints = listOf(
                    "📏 Respect strict du nombre de mots (marge de ±10%)",
                    "🚫 Interdiction de copier les phrases du texte : reformuler !",
                    "✍️ Discussion : Développer un avis personnel argumenté"
                ),
                fullContent = "# 📘 Chapitre 5 : Résumé & Discussion\nL'épreuve pratique du résumé de texte au BAC.",
                formulaSheet = "Mots du texte / 4 = Mots autorisés (±10%)",
                bacExamTips = listOf("🎯 Comptez précisément le nombre de mots et inscrivez le décompte exact à la fin de votre résumé !"),
                quizQuestions = listOf(
                    QuizQuestion("q_fr5", "Si un texte comporte 400 mots, combien de mots doit contenir le résumé au 1/4 (marge 10%) ?", listOf("50 mots", "100 mots (entre 90 et 110)", "200 mots", "300 mots"), 1, "400 / 4 = 100 mots, avec une tolérance de 90 à 110 mots.")
                )
            ),
            Chapter(
                id = "fr_ch6",
                subjectId = "fr",
                title = "Chapitre 6 : Le Commentaire Composé",
                level = "Terminale",
                series = "A",
                summary = "Organisation du commentaire composé : centres d'intérêt, procédés littéraires et effets produits.",
                keyPoints = listOf(
                    "🔍 Repérer la forme, le rythme, les figures de style",
                    "📊 Structurer en 2 ou 3 grands axes de lecture",
                    "✍️ Associer toujours un procédé stylistique à une interprétation"
                ),
                fullContent = "# 📘 Chapitre 6 : Commentaire Composé\nMéthodologie d'analyse littéraire approfondie.",
                formulaSheet = "Procédé Stylistique + Exemple du texte ➔ Effet de sens produit",
                bacExamTips = listOf("🎯 Évitez le catalogue de figures de style : expliquez en quoi la forme sert le fond !"),
                quizQuestions = listOf(
                    QuizQuestion("q_fr6", "Quelle est l'erreur majeure à éviter dans un commentaire composé ?", listOf("Citer le texte", "La paraphrase linéaire sans analyse stylistique", "Utiliser un plan en 2 parties", "Rédiger une conclusion"), 1, "La paraphrase se contente de redire le texte sans l'analyser.")
                )
            ),
            Chapter(
                id = "fr_ch7",
                subjectId = "fr",
                title = "Chapitre 7 : Figures de Style & Stylistique",
                level = "Terminale",
                series = "A",
                summary = "Catalogue et analyse des figures d'analogie, d'opposition, d'amplification et d'atténuation.",
                keyPoints = listOf(
                    "🌸 Analogie : Métaphore, Comparaison, Personnification",
                    "⚡ Opposition : Oxymore, Antithèse, Chiasme",
                    "📈 Amplification : Hyperbole, Gradation, Anaphore"
                ),
                fullContent = "# 📘 Chapitre 7 : Figures de Style\nLes outils d'analyse de la langue littéraire.",
                formulaSheet = "Comparaison (avec outil) vs Métaphore (sans outil) | Oxymore (2 mots adjacents opposés)",
                bacExamTips = listOf("🎯 L'oxymore réunit deux termes contradictoires dans un même groupe de mots (ex: 'Un obscur clarté')."),
                quizQuestions = listOf(
                    QuizQuestion("q_fr7", "Quelle figure de style associe deux mots de sens opposés dans un même groupe nominal ?", listOf("L'Anaphore", "L'Oxymore", "La Litote", "La Métonymie"), 1, "Exemple d'oxymore : 'Cette obscure clarté qui tombe des étoiles'.")
                )
            ),
            Chapter(
                id = "fr_ch8",
                subjectId = "fr",
                title = "Chapitre 8 : Grammaire & Orthographe pour le BAC",
                level = "Terminale",
                series = "A",
                summary = "Les pièges grammaticaux, concordance des temps, propositions subordonnées et qualité de rédaction.",
                keyPoints = listOf(
                    "✍️ Accord du participe passé avec l'auxiliaire avoir (si le COD est placé avant)",
                    "🔗 Subordonnées relatives, complétives et circonstancielles",
                    "✨ Ponctuation et clarté de l'expression"
                ),
                fullContent = "# 📘 Chapitre 8 : Maîtrise de la Langue\nS'exprimer sans fautes pour maximiser ses points au BAC.",
                formulaSheet = "COD avant avoir ➔ Accord | Sujet + Verbe + Compléments",
                bacExamTips = listOf("🎯 Relisez toujours votre copie 15 minutes avant la fin exclusivement pour corriger l'orthographe !"),
                quizQuestions = listOf(
                    QuizQuestion("q_fr8", "Dans la phrase 'Les fleurs qu'il a (cueillir)', comment s'accorde le participe passé ?", listOf("cueilli", "cueillies", "cueillis", "cueillie"), 1, "Le COD 'qu'' (mis pour 'les fleurs', fém. plur.) est placé avant l'auxiliaire avoir ⟹ 'cueillies'.")
                )
            ),

            // ==================== HISTOIRE-GEOGRAPHIE ====================
            Chapter(
                id = "hg_ch1",
                subjectId = "hg",
                title = "Chapitre 1 : La Côte d'Ivoire de 1960 à Nos Jours",
                level = "Terminale",
                series = "A",
                summary = "Indépendance du 7 août 1960, le miracle économique, le multipartisme de 1990 et l'émergence moderne.",
                keyPoints = listOf(
                    "📅 7 Août 1960 : Proclamation de l'Indépendance par Félix Houphouët-Boigny",
                    "🌱 Le Miracle Agricole (1960-1980) : Cacao, Café, Anacarde",
                    "🗳️ 1990 : Avènement du multipartisme et démocratisation"
                ),
                fullContent = "# 📘 Chapitre 1 : Histoire de la Côte d'Ivoire\nDe la décolonisation à l'émergence économique.",
                formulaSheet = "1960 (Indépendance) | 1990 (Multipartisme) | 1er Producteur Mondial de Cacao",
                bacExamTips = listOf("🎯 Situez toujours les événements dans leur contexte politique et socio-économique précis !"),
                quizQuestions = listOf(
                    QuizQuestion("q_hg1", "Quelle est la date précise de la proclamation de l'indépendance de la Côte d'Ivoire ?", listOf("7 août 1960", "1er novembre 1958", "12 juillet 1962", "25 mai 1963"), 0, "Félix Houphouët-Boigny a proclamé l'indépendance le 7 août 1960.")
                )
            ),
            Chapter(
                id = "hg_ch2",
                subjectId = "hg",
                title = "Chapitre 2 : L'Économie Ivoirienne & L'Émergence",
                level = "Terminale",
                series = "A",
                summary = "Le modèle agro-exportateur, la transformation locale des matières premières et les grandes infrastructures.",
                keyPoints = listOf(
                    "🍫 Cacao, Café, Anacarde, Hévéa, Palmier à huile",
                    "🏗️ Port Autonome d'Abidjan (PAA) et de San-Pédro",
                    "⚡ Énergie : Barrages d'Soubré, Singrobo et potentiel minier/pétrolier"
                ),
                fullContent = "# 📘 Chapitre 2 : Géographie Économique de la CI\nForces, contraintes et perspectives de développement de l'économie ivoirienne.",
                formulaSheet = "Secteur Primaire (Agriculture) ➔ Secteur Secondaire (Transformation) ➔ Secteur Tertiaire (Services)",
                bacExamTips = listOf("🎯 Mentionnez l'importance du PAA comme hub portuaire de la sous-région ouest-africaine !"),
                quizQuestions = listOf(
                    QuizQuestion("q_hg2", "La Côte d'Ivoire est le premier producteur mondial de quel produit agricole ?", listOf("Du Coton", "Du Cacao", "Du Blé", "Du Riz"), 1, "La Côte d'Ivoire fournit environ 40% de la production mondiale de cacao.")
                )
            ),
            Chapter(
                id = "hg_ch3",
                subjectId = "hg",
                title = "Chapitre 3 : La Décolonisation en Afrique",
                level = "Terminale",
                series = "A",
                summary = "Les facteurs de la décolonisation, les voies d'accession à l'indépendance (pacifique vs pacifique/armée) et la conférence de Bandoung.",
                keyPoints = listOf(
                    "🌍 Facteurs internes : Éveil du nationalisme, syndicalisme et intellectuels",
                    "🏛️ Facteurs externes : Seconde Guerre mondiale, anticolonialisme des USA/URSS, ONU",
                    "📜 1955 : Conférence de Bandoung et le Tiers-Monde"
                ),
                fullContent = "# 📘 Chapitre 3 : La Décolonisation\nLe processus d'émancipation des peuples africains.",
                formulaSheet = "Facteurs Internes + Facteurs Externes ➔ Accession à la Souveraineté",
                bacExamTips = listOf("🎯 Distinguez la décolonisation pacifique (AOF/AEF) des guerres de libération (Algérie, Indochine)."),
                quizQuestions = listOf(
                    QuizQuestion("q_hg3", "En quelle année s'est tenue la conférence afro-asiatique de Bandoung ?", listOf("1945", "1955", "1960", "1973"), 1, "La conférence de Bandoung s'est déroulée en avril 1955 en Indonésie.")
                )
            ),
            Chapter(
                id = "hg_ch4",
                subjectId = "hg",
                title = "Chapitre 4 : La Guerre Froide (1947-1991)",
                level = "Terminale",
                series = "A",
                summary = "L'affrontement idéologique Est-Ouest (USA vs URSS), les crises majeures (Berlin, Corée, Cuba) et la chute du bloc soviétique.",
                keyPoints = listOf(
                    "🇺🇸 USA (Capitalisme, OTAN) vs 🛠️ URSS (Communisme, Pacte de Varsovie)",
                    "🚀 Crise des missiles de Cuba (1962) : Le monde au bord du conflit nucléaire",
                    "🧱 1989 : Chute du Mur de Berlin et fin de l'URSS en 1991"
                ),
                fullContent = "# 📘 Chapitre 4 : La Guerre Froide\nL'équilibre de la terreur et la bipolarisation du monde.",
                formulaSheet = "Doctrine Truman (Containment) vs Doctrine Jdanov",
                bacExamTips = listOf("🎯 La Guerre Froide se caractérise par la formule de Raymond Aron : 'Paix impossible, guerre improbable' !"),
                quizQuestions = listOf(
                    QuizQuestion("q_hg4", "En quelle année le Mur de Berlin a-t-il été détruit ?", listOf("1961", "1973", "1989", "1991"), 2, "La chute du Mur de Berlin le 9 novembre 1989 symbolise la fin de la Guerre Froide.")
                )
            ),
            Chapter(
                id = "hg_ch5",
                subjectId = "hg",
                title = "Chapitre 5 : La Mondialisation Économique",
                level = "Terminale",
                series = "A",
                summary = "Les acteurs de la mondialisation (FMN, OMC, FMI), la maritimisation du commerce mondial et les inégalités de développement.",
                keyPoints = listOf(
                    "🚢 Maritimisation : 90% du commerce international transite par mer",
                    "🏢 Firmes Multinationales (FMN) et divisions internationales du travail",
                    "🌐 Les centres de décision : Triade (Amérique du Nord, Europe, Asie de l'Est)"
                ),
                fullContent = "# 📘 Chapitre 5 : La Mondialisation\nL'interconnexion croissante des économies mondiales.",
                formulaSheet = "FTN + OMC + Réseaux de Transports ➔ Flux de Marchandises, Capitaux et Informations",
                bacExamTips = listOf("🎯 N'oubliez pas les espaces marginaux de la mondialisation (les Pays les Moins Avancés) !"),
                quizQuestions = listOf(
                    QuizQuestion("q_hg5", "Quel pourcentage du commerce mondial est transporté par voie maritime ?", listOf("25%", "50%", "75%", "Environ 90%"), 3, "La voie maritime reste le cœur battant du commerce international.")
                )
            ),
            Chapter(
                id = "hg_ch6",
                subjectId = "hg",
                title = "Chapitre 6 : Les Grandes Puissances (USA & Chine)",
                level = "Terminale",
                series = "A",
                summary = "L'hyperpuissance américaine et l'émergence spectaculaire de la Chine comme usine du monde.",
                keyPoints = listOf(
                    "🇺🇸 USA : Hard Power (armée, dollar) et Soft Power (Hollywood, tech)",
                    "🇨🇳 Chine : 2ème puissance économique mondiale, routes de la soie (BRI)",
                    "⚡ Rivalités commerciales et technologiques"
                ),
                fullContent = "# 📘 Chapitre 6 : USA et Chine\nComprendre les deux géants de la géopolitique du XXIe siècle.",
                formulaSheet = "USA (Hard Power + Soft Power) vs Chine (Atelier du monde + Nouvelle Route de la Soie)",
                bacExamTips = listOf("🎯 Comparez l'organisation spatiale des mégalopoles américaine (BosWash) et chinoise !"),
                quizQuestions = listOf(
                    QuizQuestion("q_hg6", "Comment appelle-t-on la Mégalopole américaine s'étendant de Boston à Washington ?", listOf("La Sunbelt", "BosWash", "Silicon Valley", "Rust Belt"), 1, "BosWash est la plus grande mégalopole du monde avec plus de 50 millions d'habitants.")
                )
            ),
            Chapter(
                id = "hg_ch7",
                subjectId = "hg",
                title = "Chapitre 7 : L'Union Africaine & La CEDEAO",
                level = "Terminale",
                series = "A",
                summary = "Les enjeux de l'intégration régionale en Afrique de l'Ouest, libre circulation, ZLECAF et défis sécuritaires.",
                keyPoints = listOf(
                    "🌍 CEDEAO (1975) : Communauté Économique des États de l'Afrique de l'Ouest",
                    "📜 ZLECAF : Zone de Libre-Échange Continentale Africaine",
                    "🕊️ Rôle de la diplomatie ivoirienne dans la sous-région"
                ),
                fullContent = "# 📘 Chapitre 7 : Intégration Régionale en Afrique\nVers un marché commun et une monnaie unique.",
                formulaSheet = "UA (Addis-Abeba) | CEDEAO (Abuja) | ZLECAF",
                bacExamTips = listOf("🎯 La libre circulation des personnes et des biens est un pilier fondamental de la CEDEAO !"),
                quizQuestions = listOf(
                    QuizQuestion("q_hg7", "En quelle année la CEDEAO a-t-elle été créée ?", listOf("1960", "1975", "1999", "2010"), 1, "La CEDEAO a été créée le 28 mai 1975 par le traité de Lagos.")
                )
            ),
            Chapter(
                id = "hg_ch8",
                subjectId = "hg",
                title = "Chapitre 8 : Méthodologie du Commentaire en HG",
                level = "Terminale",
                series = "A",
                summary = "Savoir lire une carte, analyser un graphique, calculer des taux de croissance et commenter un texte historique.",
                keyPoints = listOf(
                    "📊 Graphique : Courbes, histogrammes, diagrammes circulaires",
                    "🗺️ Carte : Titre, Légende, Échelle, Orientation (TLEO)",
                    "✍️ Rédaction de l'introduction, des parties explicatives et de la conclusion"
                ),
                fullContent = "# 📘 Chapitre 8 : Méthode du Commentaire de Documents\nGuide pratique des épreuves de cartographie et de statistiques au BAC.",
                formulaSheet = "Taux de variation = [(Valeur Finale - Valeur Initiale) / Valeur Initiale] × 100",
                bacExamTips = listOf("🎯 Une carte sans légende ou sans titre perd automatiquement la moitié des points au barème !"),
                quizQuestions = listOf(
                    QuizQuestion("q_hg8", "Quels sont les 4 éléments obligatoires d'une carte géographique réussie ?", listOf("Couleurs, Gomme, Crayon, Règle", "Titre, Légende, Échelle, Orientation", "Pays, Capitale, Fleuve, Mer", "Auteur, Date, Éditeur, Prix"), 1, "Règle TLEO : Titre, Légende, Échelle, Orientation.")
                )
            ),

            // ==================== ANGLAIS ====================
            Chapter(
                id = "en_ch1",
                subjectId = "en",
                title = "Chapter 1: Reading Comprehension Techniques",
                level = "Terminale",
                series = "A",
                summary = "Master skimming, scanning, context clues and answering True/False questions with precise text justification.",
                keyPoints = listOf(
                    "📖 Skimming for main idea & Scanning for specific facts",
                    "✅ True / False questions: Always quote the exact line numbers!",
                    "🔗 Word reference: Identifying what 'it', 'they', or 'which' refers to"
                ),
                fullContent = "# 📘 Chapter 1: Reading Comprehension Guide\nHow to get maximum points in Part I of the English BAC exam.",
                formulaSheet = "True/False + Quote: '...' (lines X-Y)",
                bacExamTips = listOf("🎯 Never forget quotation marks when justifying your True/False answers!"),
                quizQuestions = listOf(
                    QuizQuestion("q_en1", "What technique is used to find specific details like dates or numbers quickly?", listOf("Skimming", "Scanning", "Translating", "Proofreading"), 1, "Scanning means searching rapidly for a specific word or number.")
                )
            ),
            Chapter(
                id = "en_ch2",
                subjectId = "en",
                title = "Chapter 2: Essay Writing for BAC",
                level = "Terminale",
                series = "A",
                summary = "Structuring argumentative essays, writing clear introduction hooks, body paragraphs and conclusions.",
                keyPoints = listOf(
                    "✍️ Structure: Introduction ➔ Body Paragraph 1 ➔ Body Paragraph 2 ➔ Conclusion",
                    "🔗 Connectors: First of all, In addition, On the other hand, To conclude",
                    "📝 Word count: Respect the 120-150 word requirement"
                ),
                fullContent = "# 📘 Chapter 2: Essay Writing Strategy\nExpressing your opinions clearly in written English.",
                formulaSheet = "Intro (Hook + Thesis statement) | Body (Point + Example) | Conclusion",
                bacExamTips = listOf("🎯 Always check subject-verb agreement and tenses before giving back your paper!"),
                quizQuestions = listOf(
                    QuizQuestion("q_en2", "Which connector is best to introduce a concluding thought?", listOf("However", "To sum up", "Furthermore", "For instance"), 1, "'To sum up' or 'To conclude' is ideal for the final paragraph.")
                )
            ),
            Chapter(
                id = "en_ch3",
                subjectId = "en",
                title = "Chapter 3: Grammar Mastery - Tenses & Conditionals",
                level = "Terminale",
                series = "A",
                summary = "Mastering Present Perfect vs Past Simple, Conditionals (If-clauses) and Passive Voice.",
                keyPoints = listOf(
                    "⏱️ Present Perfect: Have/Has + Past Participle (Action connected to present)",
                    "⚡ Conditionals: Type 1 (Real), Type 2 (Unreal present), Type 3 (Unreal past)",
                    "🔄 Passive Voice: Object + Be + Past Participle + By + Subject"
                ),
                fullContent = "# 📘 Chapter 3: English Grammar Rules\nEssential grammatical structures for BAC rewriting exercises.",
                formulaSheet = "If + Present, Will + Verb | If + Past, Would + Verb | If + Past Perfect, Would Have + PP",
                bacExamTips = listOf("🎯 In Conditional Type 2, use 'were' for all subjects (e.g., 'If I were you...')."),
                quizQuestions = listOf(
                    QuizQuestion("q_en3", "Complete: 'If I ___ enough money, I would buy a computer.'", listOf("have", "had", "would have", "will have"), 1, "Conditional Type 2: If + Past Simple, Would + Infinitive.")
                )
            ),
            Chapter(
                id = "en_ch4",
                subjectId = "en",
                title = "Chapter 4: Reported Speech & Relative Clauses",
                level = "Terminale",
                series = "A",
                summary = "Transforming direct speech into reported speech and combining sentences with Who, Which, Where, Whose.",
                keyPoints = listOf(
                    "🗣️ Direct to Reported: Tenses shift back (Present Simple ➔ Past Simple)",
                    "🔗 Relative Pronouns: Who (People), Which (Things), Whose (Possession), Where (Place)",
                    "⏱️ Time expressions change: 'Tomorrow' ➔ 'The following day'"
                ),
                fullContent = "# 📘 Chapter 4: Reported Speech & Relative Clauses\nMastering sentence transformation exercises.",
                formulaSheet = "Direct: 'I am tired' ➔ Reported: He said that he was tired.",
                bacExamTips = listOf("🎯 Don't forget to change pronouns and time markers in reported speech!"),
                quizQuestions = listOf(
                    QuizQuestion("q_en4", "Choose the correct pronoun: 'The student ___ won the prize is from Yamoussoukro.'", listOf("which", "who", "whose", "where"), 1, "'Who' is used for people.")
                )
            ),
            Chapter(
                id = "en_ch5",
                subjectId = "en",
                title = "Chapter 5: Vocabulary - Technology & Environment",
                level = "Terminale",
                series = "A",
                summary = "Key vocabulary related to artificial intelligence, climate change, renewable energy and deforestation.",
                keyPoints = listOf(
                    "🌱 Environment: Global warming, Pollution, Renewable energy, Sustainable development",
                    "💻 Tech & AI: Innovations, Cyberbullying, Digital divide, Social media addiction",
                    "📜 Synonyms and antonyms for reading texts"
                ),
                fullContent = "# 📘 Chapter 5: Essential Vocabulary\nTheme-based word lists for reading and writing.",
                formulaSheet = "Global Warming | Renewable Energy | Artificial Intelligence | Social Media",
                bacExamTips = listOf("🎯 Learn collocations like 'raise awareness', 'take measures', 'play a crucial role'!"),
                quizQuestions = listOf(
                    QuizQuestion("q_en5", "What is the synonym of 'global warming'?", listOf("Deforestation", "Climate change", "Biodiversity", "Recycling"), 1, "'Climate change' is closely related to global warming.")
                )
            ),
            Chapter(
                id = "en_ch6",
                subjectId = "en",
                title = "Chapter 6: Social Issues in West Africa",
                level = "Terminale",
                series = "A",
                summary = "Discussing youth unemployment, female education, immigration and entrepreneurship in West Africa.",
                keyPoints = listOf(
                    "🎓 Female empowerment and access to education",
                    "💼 Entrepreneurship and job creation for young graduates",
                    "🌍 Brain drain vs Brain gain in African nations"
                ),
                fullContent = "# 📘 Chapter 6: Social Issues in West Africa\nKey topics frequently featured in BAC text passages.",
                formulaSheet = "Youth Unemployment | Female Empowerment | Brain Drain | Entrepreneurship",
                bacExamTips = listOf("🎯 Use examples from Côte d'Ivoire when writing your opinion essay!"),
                quizQuestions = listOf(
                    QuizQuestion("q_en6", "What term describes the emigration of trained and educated people from developing nations?", listOf("Brain drain", "Globalization", "Urbanization", "Brainstorming"), 0, "'Brain drain' refers to the loss of educated human resources.")
                )
            ),
            Chapter(
                id = "en_ch7",
                subjectId = "en",
                title = "Chapter 7: BAC Oral Examination Preparation",
                level = "Terminale",
                series = "A",
                summary = "Tips and phrases to succeed in the oral English exam (BAC Orale): text presentation, questioning and discussion.",
                keyPoints = listOf(
                    "🎙️ Step 1: Introduce the text (Title, Author, Source, Main Topic)",
                    "🎙️ Step 2: Detailed summary of paragraph by paragraph",
                    "🎙️ Step 3: Personal commentary and answering the examiner's questions"
                ),
                fullContent = "# 📘 Chapter 7: Oral Exam Guide\nHow to speak confidently in front of the English examiner.",
                formulaSheet = "'This document is an excerpt from...', 'The author deals with...', 'In my opinion...'",
                bacExamTips = listOf("🎯 Speak clearly, make eye contact and don't panic if you make a small mistake: correct yourself smoothly!"),
                quizQuestions = listOf(
                    QuizQuestion("q_en7", "How should you start your presentation during the English oral exam?", listOf("By saying goodbye", "By introducing the title, author and main topic of the text", "By singing", "By giving your final mark"), 1, "Always introduce the document credentials first.")
                )
            ),
            Chapter(
                id = "en_ch8",
                subjectId = "en",
                title = "Chapter 8: BAC Annales & Error Correction",
                level = "Terminale",
                series = "A",
                summary = "Reviewing past BAC exam papers, avoiding common errors made by Francophone students and final revision checklist.",
                keyPoints = listOf(
                    "⚠️ False friends (e.g. 'actually' means 'en fait', not 'actuellement')",
                    "✍️ Prepositions: 'depend on' (not 'depend of'), 'listen to' (not 'listen')",
                    "⏱️ Time management during the 3-hour BAC paper"
                ),
                fullContent = "# 📘 Chapter 8: Past Papers & Common Mistakes\nFinal polish before taking the BAC exam.",
                formulaSheet = "Depend ON | Listen TO | Discuss SOMETHING | Look AT",
                bacExamTips = listOf("🎯 Remember: 'actually' means 'in fact'! 'Currently' is the word for 'actuellement'."),
                quizQuestions = listOf(
                    QuizQuestion("q_en8", "Which preposition goes with the verb 'depend'?", listOf("of", "on", "in", "at"), 1, "In English we say 'it depends ON', never 'of'.")
                )
            ),

            // ==================== LV2 (ALLEMAND / ESPAGNOL) ====================
            Chapter(
                id = "lv2_ch1",
                subjectId = "lv2",
                title = "Chapitre 1 : Compréhension Écrite & Vocabulaire LV2",
                level = "Terminale",
                series = "A",
                summary = "Stratégies de lecture de textes en Allemand/Espagnol pour les épreuves du BAC.",
                keyPoints = listOf("📖 Repérage des idées clés et connecteurs logiques", "✍️ Expression écrite et tournures idiomatiques"),
                fullContent = "# 📘 Chapitre 1 : LV2 BAC Guide\nMéthodes de lecture et réponses argumentées en Allemand et Espagnol.",
                formulaSheet = "Analyse de texte + Rédaction guidée",
                bacExamTips = listOf("🎯 Révisez les verbes irréguliers fondamentaux avant le jour J !"),
                quizQuestions = listOf(
                    QuizQuestion("q_lv2_1", "Quelle démarche adopter face à un texte en LV2 ?", listOf("Traduire mot à mot", "Lire d'abord les questions pour guider la lecture", "Sauter le texte", "Deviner"), 1, "Lire les questions au préalable permet de cibler les informations essentielles.")
                )
            ),

            // ==================== EPS ====================
            Chapter(
                id = "eps_ch1",
                subjectId = "eps",
                title = "Chapitre 1 : Éducation Physique & Santé au BAC",
                level = "Terminale",
                series = "D",
                summary = "Règlements des épreuves pratiques (Gymnastique, Athlétisme, Sports collectifs), physiologie et hygiène de vie.",
                keyPoints = listOf("🏃 Hydratation, échauffement et prévention des blessures", "⚽ Règlements officiels des épreuves du BAC EPS"),
                fullContent = "# 📘 Chapitre 1 : EPS & Physiologie du Sport\nSe préparer physiquement et théoriquement aux épreuves sportives du BAC.",
                formulaSheet = "Échauffement ➔ Épreuve ➔ Étirements ➔ Récupération",
                bacExamTips = listOf("🎯 Effectuez toujours un échauffement complet de 15 minutes avant le début de l'épreuve !"),
                quizQuestions = listOf(
                    QuizQuestion("q_eps1", "Combien de temps doit durer un échauffement musculaire et articulaire efficace ?", listOf("1 minute", "Entre 10 et 15 minutes", "1 heure", "Pas besoin"), 1, "Un échauffement progressif de 10 à 15 minutes prépare l'organisme et évite les lésions.")
                )
            )
        )
    }

    fun getDefaultHistorique(): List<com.example.data.models.HistoriqueEntity> {
        val now = System.currentTimeMillis()
        val dayMs = 24 * 60 * 60 * 1000L
        return listOf(
            com.example.data.models.HistoriqueEntity(
                id = 1,
                subjectId = "math",
                subjectName = "Mathématiques",
                chapterId = "math_ch1",
                chapterTitle = "Limites et Continuité",
                score = 3,
                totalQuestions = 5,
                scorePercentage = 60,
                revisionDate = now - (6 * dayMs) + (10 * 3600 * 1000L),
                revisionType = "QUIZ",
                durationSeconds = 110,
                feedback = "Bon début. Revoir le calcul de limites indéterminées 0/0."
            ),
            com.example.data.models.HistoriqueEntity(
                id = 2,
                subjectId = "physique",
                subjectName = "Physique-Chimie",
                chapterId = "pc_ch1",
                chapterTitle = "Cinématique & Lois de Newton",
                score = 3,
                totalQuestions = 4,
                scorePercentage = 75,
                revisionDate = now - (5 * dayMs) + (14 * 3600 * 1000L),
                revisionType = "QUIZ",
                durationSeconds = 135,
                feedback = "Bien assimilé. Attention aux projections vectorielles."
            ),
            com.example.data.models.HistoriqueEntity(
                id = 3,
                subjectId = "svt",
                subjectName = "SVT",
                chapterId = "svt_ch1",
                chapterTitle = "Génétique et Hérédité Humaine",
                score = 4,
                totalQuestions = 5,
                scorePercentage = 80,
                revisionDate = now - (4 * dayMs) + (16 * 3600 * 1000L),
                revisionType = "QUIZ",
                durationSeconds = 95,
                feedback = "Très bonne maîtrise des croisements génétiques."
            ),
            com.example.data.models.HistoriqueEntity(
                id = 4,
                subjectId = "math",
                subjectName = "Mathématiques",
                chapterId = "math_ch2",
                chapterTitle = "Fonction Logarithme Népérien",
                score = 4,
                totalQuestions = 5,
                scorePercentage = 80,
                revisionDate = now - (3 * dayMs) + (11 * 3600 * 1000L),
                revisionType = "REVISION",
                durationSeconds = 150,
                feedback = "Propriétés algébriques du logarithme bien maîtrisées."
            ),
            com.example.data.models.HistoriqueEntity(
                id = 5,
                subjectId = "philo",
                subjectName = "Philosophie",
                chapterId = "philo_ch1",
                chapterTitle = "L'Humain et la Société",
                score = 4,
                totalQuestions = 5,
                scorePercentage = 80,
                revisionDate = now - (2 * dayMs) + (18 * 3600 * 1000L),
                revisionType = "QUIZ",
                durationSeconds = 120,
                feedback = "Excellente analyse conceptuelle."
            ),
            com.example.data.models.HistoriqueEntity(
                id = 6,
                subjectId = "physique",
                subjectName = "Physique-Chimie",
                chapterId = "pc_ch2",
                chapterTitle = "Acides et Bases en Solution",
                score = 5,
                totalQuestions = 5,
                scorePercentage = 100,
                revisionDate = now - (1 * dayMs) + (9 * 3600 * 1000L),
                revisionType = "QUIZ",
                durationSeconds = 90,
                feedback = "Score parfait ! Équilibre chimique et pH maîtrisés."
            ),
            com.example.data.models.HistoriqueEntity(
                id = 7,
                subjectId = "math",
                subjectName = "Mathématiques",
                chapterId = "math_ch3",
                chapterTitle = "Nombres Complexes & Géométrie",
                score = 5,
                totalQuestions = 5,
                scorePercentage = 100,
                revisionDate = now - (3 * 3600 * 1000L),
                revisionType = "QUIZ",
                durationSeconds = 85,
                feedback = "Score parfait ! Forme trigonométrique et arguments au top."
            )
        )
    }

}
