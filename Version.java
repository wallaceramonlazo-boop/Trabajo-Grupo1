import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.geom.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class RelacionDivisibilidad {

    private final int n;
    private final List<Integer> D;
    private List<Integer> A;

    public RelacionDivisibilidad(int n) {
        this.n = n;
        this.D = calcularDivisores();
        this.A = new ArrayList<>();
    }

    // ── Lógica del dominio ─────────────────────────────────────────────────────

    private List<Integer> calcularDivisores() {
        List<Integer> divs = new ArrayList<>();
        for (int i = 1; i <= n; i++) {
            if (n % i == 0) divs.add(i);
        }
        return divs;
    }

    private boolean esCobertura(int a, int b) {
        return A.stream().noneMatch(c -> c != a && c != b && c % a == 0 && b % c == 0);
    }

    private List<int[]> pares() {
        List<int[]> result = new ArrayList<>();
        for (int a : A)
            for (int b : A)
                if (a != b && b % a == 0)
                    result.add(new int[]{a, b});
        return result;
    }

    private List<int[]> cobertura() {
        return pares().stream()
                .filter(p -> esCobertura(p[0], p[1]))
                .collect(Collectors.toList());
    }

    private Map<Integer, Integer> niveles() {
        Map<Integer, Integer> niv = new LinkedHashMap<>();
        for (int a : A) niv.put(a, 0);
        boolean cambio = true;
        while (cambio) {
            cambio = false;
            for (int[] par : cobertura()) {
                int a = par[0], b = par[1];
                if (niv.get(b) <= niv.get(a)) {
                    niv.put(b, niv.get(a) + 1);
                    cambio = true;
                }
            }
        }
        return niv;
    }

    private List<Integer> maximales() {
        return A.stream()
                .filter(a -> A.stream().noneMatch(b -> b != a && b % a == 0))
                .collect(Collectors.toList());
    }

    private List<Integer> minimales() {
        return A.stream()
                .filter(a -> A.stream().noneMatch(b -> b != a && a % b == 0))
                .collect(Collectors.toList());
    }

    private Map<Integer, double[]> posiciones() {
        Map<Integer, Integer> niv = niveles();
        Map<Integer, List<Integer>> grupos = new TreeMap<>();
        for (int a : A) grupos.computeIfAbsent(niv.get(a), k -> new ArrayList<>()).add(a);
        Map<Integer, double[]> pos = new HashMap<>();
        for (Map.Entry<Integer, List<Integer>> entry : grupos.entrySet()) {
            int nivel = entry.getKey();
            List<Integer> nodos = entry.getValue();
            Collections.sort(nodos);
            int total = nodos.size();
            for (int i = 0; i < total; i++) {
                double x = (i - (total - 1) / 2.0) * 2.0;
                pos.put(nodos.get(i), new double[]{x, nivel});
            }
        }
        return pos;
    }

    // ── Selección del subconjunto ──────────────────────────────────────────────

    public void seleccionarTodo() {
        A = new ArrayList<>(D);
    }

    public boolean seleccionarManual(String input) {
        A = new ArrayList<>();
        String[] partes = input.trim().split("[,\\s]+");
        for (String p : partes) {
            try {
                int x = Integer.parseInt(p.trim());
                if (D.contains(x)) {
                    A.add(x);
                } else {
                    return false; // elemento inválido
                }
            } catch (NumberFormatException e) {
                return false;
            }
        }
        Collections.sort(A);
        return !A.isEmpty();
    }

    // ── Texto para consola/panel ───────────────────────────────────────────────

    public String resumenTexto() {
        Map<Integer, Integer> niv = niveles();
        StringBuilder sb = new StringBuilder();
        sb.append("D(").append(n).append(") = ").append(D).append("\n");
        sb.append("A            = ").append(A).append("\n");
        sb.append("Pares (a|b)  = ").append(
                pares().stream().map(p -> "(" + p[0] + "," + p[1] + ")").collect(Collectors.joining(", "))
        ).append("\n");
        sb.append("Maximales    : ").append(maximales()).append("\n");
        sb.append("Minimales    : ").append(minimales()).append("\n");
        sb.append("\n── Diagrama de Hasse (texto) ──\n");
        if (!niv.isEmpty()) {
            int maxNiv = Collections.max(niv.values());
            for (int lvl = maxNiv; lvl >= 0; lvl--) {
                final int fl = lvl;
                List<Integer> nodos = A.stream().filter(a -> niv.get(a) == fl).collect(Collectors.toList());
                sb.append("  Niv ").append(lvl).append(": ").append(nodos).append("\n");
                if (lvl > 0) {
                    List<String> aristas = cobertura().stream()
                            .filter(p -> niv.get(p[1]) == fl)
                            .map(p -> p[0] + "→" + p[1])
                            .collect(Collectors.toList());
                    sb.append("  aristas: ").append(aristas).append("\n");
                }
            }
        }
        return sb.toString();
    }

    // Getters para el panel gráfico
    public int getN() { return n; }
    public List<Integer> getD() { return D; }
    public List<Integer> getA() { return A; }
    public List<int[]> getCobertura() { return cobertura(); }
    public Map<Integer, double[]> getPosiciones() { return posiciones(); }
    public List<Integer> getMaximales() { return maximales(); }
    public List<Integer> getMinimales() { return minimales(); }

    // ══════════════════════════════════════════════════════════════════════════
    //  GUI
    // ══════════════════════════════════════════════════════════════════════════

    static class HasseDiagramPanel extends JPanel {

        private RelacionDivisibilidad rel;

        private static final Color COLOR_MIN   = new Color(33,  150, 243);
        private static final Color COLOR_MID   = new Color(255, 152,   0);
        private static final Color COLOR_MAX   = new Color(76,  175,  80);
        private static final Color COLOR_EDGE  = new Color(100, 100, 100);
        private static final Color COLOR_BG    = new Color(250, 250, 252);
        private static final int   RADIUS      = 22;

        HasseDiagramPanel() {
            setBackground(COLOR_BG);
            setPreferredSize(new Dimension(600, 480));
        }

        void setRelacion(RelacionDivisibilidad r) {
            this.rel = r;
            repaint();
        }

        private Point toScreen(double x, double y, Map<Integer, double[]> pos) {
            // Compute bounding box then map to panel with margins
            double minX = Double.MAX_VALUE, maxX = -Double.MAX_VALUE;
            double minY = Double.MAX_VALUE, maxY = -Double.MAX_VALUE;
            for (double[] p : pos.values()) {
                if (p[0] < minX) minX = p[0];
                if (p[0] > maxX) maxX = p[0];
                if (p[1] < minY) minY = p[1];
                if (p[1] > maxY) maxY = p[1];
            }
            int margin = 60;
            int W = getWidth()  - 2 * margin;
            int H = getHeight() - 2 * margin;
            double rangeX = maxX == minX ? 1 : maxX - minX;
            double rangeY = maxY == minY ? 1 : maxY - minY;
            int sx = margin + (int)((x - minX) / rangeX * W);
            int sy = getHeight() - margin - (int)((y - minY) / rangeY * H);
            return new Point(sx, sy);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (rel == null || rel.getA().isEmpty()) {
                g.setColor(new Color(180, 180, 200));
                g.setFont(new Font("Serif", Font.ITALIC, 16));
                String msg = "Selecciona n y un subconjunto para graficar";
                FontMetrics fm = g.getFontMetrics();
                g.drawString(msg, (getWidth() - fm.stringWidth(msg)) / 2, getHeight() / 2);
                return;
            }

            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            Map<Integer, double[]> pos  = rel.getPosiciones();
            List<int[]>            cob  = rel.getCobertura();
            List<Integer>          maxs = rel.getMaximales();
            List<Integer>          mins = rel.getMinimales();

            // Draw edges
            g2.setStroke(new BasicStroke(1.8f));
            g2.setColor(COLOR_EDGE);
            for (int[] par : cob) {
                double[] pa = pos.get(par[0]);
                double[] pb = pos.get(par[1]);
                if (pa == null || pb == null) continue;
                Point from = toScreen(pa[0], pa[1], pos);
                Point to   = toScreen(pb[0], pb[1], pos);
                g2.drawLine(from.x, from.y, to.x, to.y);
            }

            // Draw nodes
            for (Map.Entry<Integer, double[]> entry : pos.entrySet()) {
                int nodo = entry.getKey();
                double[] xy = entry.getValue();
                Point p = toScreen(xy[0], xy[1], pos);

                Color fill;
                if (maxs.contains(nodo))      fill = COLOR_MAX;
                else if (mins.contains(nodo)) fill = COLOR_MIN;
                else                           fill = COLOR_MID;

                // Shadow
                g2.setColor(new Color(0, 0, 0, 40));
                g2.fillOval(p.x - RADIUS + 2, p.y - RADIUS + 2, RADIUS * 2, RADIUS * 2);

                // Circle
                g2.setColor(fill);
                g2.fillOval(p.x - RADIUS, p.y - RADIUS, RADIUS * 2, RADIUS * 2);

                // Border
                g2.setColor(fill.darker());
                g2.setStroke(new BasicStroke(2f));
                g2.drawOval(p.x - RADIUS, p.y - RADIUS, RADIUS * 2, RADIUS * 2);

                // Label
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("SansSerif", Font.BOLD, 13));
                FontMetrics fm = g2.getFontMetrics();
                String label = String.valueOf(nodo);
                g2.drawString(label, p.x - fm.stringWidth(label) / 2, p.y + fm.getAscent() / 2 - 1);
            }

            // Legend
            drawLegend(g2);
        }

        private void drawLegend(Graphics2D g2) {
            int lx = 10, ly = getHeight() - 75;
            String[] labels = {"Minimal", "Intermedio", "Maximal"};
            Color[]  colors = {COLOR_MIN, COLOR_MID, COLOR_MAX};
            g2.setFont(new Font("SansSerif", Font.PLAIN, 11));
            for (int i = 0; i < 3; i++) {
                g2.setColor(colors[i]);
                g2.fillOval(lx, ly + i * 20, 12, 12);
                g2.setColor(new Color(60, 60, 60));
                g2.drawString(labels[i], lx + 17, ly + i * 20 + 11);
            }
        }
    }

    // ── Main Window ────────────────────────────────────────────────────────────

    static class MainWindow extends JFrame {

        private final JSpinner        spinnerN;
        private final JTextArea       textInfo;
        private final HasseDiagramPanel diagramPanel;
        private final JRadioButton    rbTodo, rbManual;
        private final JTextField      fieldManual;
        private final JButton         btnCalcular;

        MainWindow() {
            super("Relación de Divisibilidad — Diagrama de Hasse");
            setDefaultCloseOperation(EXIT_ON_CLOSE);
            setLayout(new BorderLayout(8, 8));
            getContentPane().setBackground(new Color(240, 242, 248));

            // ── TOP PANEL ─────────────────────────────────────────────────────
            JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
            topPanel.setBackground(new Color(30, 40, 70));
            topPanel.setBorder(new EmptyBorder(4, 8, 4, 8));

            JLabel lblTitle = new JLabel("Divisibilidad  |  Hasse");
            lblTitle.setFont(new Font("Serif", Font.BOLD, 18));
            lblTitle.setForeground(Color.WHITE);
            topPanel.add(lblTitle);

            add(topPanel, BorderLayout.NORTH);

            // ── LEFT CONTROL PANEL ────────────────────────────────────────────
            JPanel leftPanel = new JPanel();
            leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
            leftPanel.setBackground(new Color(245, 246, 250));
            leftPanel.setBorder(new CompoundBorder(
                    new EmptyBorder(10, 10, 10, 6),
                    new CompoundBorder(
                            BorderFactory.createLineBorder(new Color(200, 205, 220), 1, true),
                            new EmptyBorder(12, 12, 12, 12)
                    )
            ));
            leftPanel.setPreferredSize(new Dimension(230, 0));

            // N input
            JLabel lblN = new JLabel("Valor de n:");
            lblN.setFont(new Font("SansSerif", Font.BOLD, 12));
            lblN.setAlignmentX(Component.LEFT_ALIGNMENT);
            spinnerN = new JSpinner(new SpinnerNumberModel(12, 1, 10000, 1));
            spinnerN.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
            spinnerN.setAlignmentX(Component.LEFT_ALIGNMENT);

            // Subset options
            JLabel lblSub = new JLabel("Subconjunto A:");
            lblSub.setFont(new Font("SansSerif", Font.BOLD, 12));
            lblSub.setAlignmentX(Component.LEFT_ALIGNMENT);
            rbTodo   = new JRadioButton("Todo D(n)", true);
            rbManual = new JRadioButton("Manual");
            ButtonGroup bg = new ButtonGroup();
            bg.add(rbTodo); bg.add(rbManual);
            rbTodo.setBackground(new Color(245, 246, 250));
            rbManual.setBackground(new Color(245, 246, 250));
            rbTodo.setAlignmentX(Component.LEFT_ALIGNMENT);
            rbManual.setAlignmentX(Component.LEFT_ALIGNMENT);

            JLabel lblManualHint = new JLabel("<html><small>Ej: 1, 2, 4, 6</small></html>");
            lblManualHint.setAlignmentX(Component.LEFT_ALIGNMENT);
            fieldManual = new JTextField();
            fieldManual.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
            fieldManual.setAlignmentX(Component.LEFT_ALIGNMENT);
            fieldManual.setEnabled(false);

            rbManual.addActionListener(e -> fieldManual.setEnabled(true));
            rbTodo  .addActionListener(e -> fieldManual.setEnabled(false));

            btnCalcular = new JButton("▶  Calcular");
            btnCalcular.setFont(new Font("SansSerif", Font.BOLD, 13));
            btnCalcular.setBackground(new Color(30, 40, 70));
            btnCalcular.setForeground(Color.WHITE);
            btnCalcular.setFocusPainted(false);
            btnCalcular.setBorder(new EmptyBorder(8, 14, 8, 14));
            btnCalcular.setAlignmentX(Component.LEFT_ALIGNMENT);
            btnCalcular.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
            btnCalcular.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            btnCalcular.addActionListener(e -> calcular());

            leftPanel.add(lblN);
            leftPanel.add(Box.createVerticalStrut(4));
            leftPanel.add(spinnerN);
            leftPanel.add(Box.createVerticalStrut(14));
            leftPanel.add(lblSub);
            leftPanel.add(Box.createVerticalStrut(4));
            leftPanel.add(rbTodo);
            leftPanel.add(rbManual);
            leftPanel.add(Box.createVerticalStrut(4));
            leftPanel.add(lblManualHint);
            leftPanel.add(Box.createVerticalStrut(4));
            leftPanel.add(fieldManual);
            leftPanel.add(Box.createVerticalStrut(20));
            leftPanel.add(btnCalcular);
            leftPanel.add(Box.createVerticalGlue());

            add(leftPanel, BorderLayout.WEST);

            // ── CENTER: diagram ────────────────────────────────────────────────
            diagramPanel = new HasseDiagramPanel();
            diagramPanel.setBorder(new CompoundBorder(
                    new EmptyBorder(10, 4, 10, 10),
                    BorderFactory.createLineBorder(new Color(200, 205, 220), 1, true)
            ));
            add(diagramPanel, BorderLayout.CENTER);

            // ── BOTTOM: text info ──────────────────────────────────────────────
            textInfo = new JTextArea(7, 40);
            textInfo.setEditable(false);
            textInfo.setFont(new Font("Monospaced", Font.PLAIN, 12));
            textInfo.setBackground(new Color(24, 28, 40));
            textInfo.setForeground(new Color(180, 220, 180));
            textInfo.setCaretColor(Color.WHITE);
            textInfo.setBorder(new EmptyBorder(8, 10, 8, 10));
            JScrollPane scrollPane = new JScrollPane(textInfo);
            scrollPane.setBorder(new EmptyBorder(0, 10, 10, 10));
            add(scrollPane, BorderLayout.SOUTH);

            pack();
            setMinimumSize(new Dimension(820, 600));
            setLocationRelativeTo(null);
        }

        private void calcular() {
            int nVal = (int) spinnerN.getValue();
            RelacionDivisibilidad rel = new RelacionDivisibilidad(nVal);

            if (rbTodo.isSelected()) {
                rel.seleccionarTodo();
            } else {
                String input = fieldManual.getText().trim();
                if (input.isEmpty()) {
                    JOptionPane.showMessageDialog(this,
                            "Ingresa elementos separados por coma.\nEj: 1, 2, 4",
                            "Entrada vacía", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                if (!rel.seleccionarManual(input)) {
                    JOptionPane.showMessageDialog(this,
                            "Uno o más elementos no pertenecen a D(" + nVal + ").\n" +
                            "D(" + nVal + ") = " + rel.getD(),
                            "Elemento inválido", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }

            textInfo.setText(rel.resumenTexto());
            diagramPanel.setRelacion(rel);
        }
    }

    // ── Entry point ────────────────────────────────────────────────────────────

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
            catch (Exception ignored) {}
            new MainWindow().setVisible(true);
        });
    }
}
