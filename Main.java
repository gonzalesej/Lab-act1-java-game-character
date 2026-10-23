import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.ParseException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.function.Consumer;

public class Main extends JFrame {

    // ---------- Theme ----------
    private static final Color BG = new Color(24, 26, 38);
    private static final Color PANEL = new Color(36, 39, 58);
    private static final Color LINE = new Color(55, 58, 80);
    private static final Color TEXT = new Color(235, 235, 245);
    private static final Color GOLD = new Color(255, 196, 61);

    // ---------- Data (ADTs) ----------
    private final CharacterList roster = new CharacterList();          // linked list ADT
    private final Deque<GameCharacter> recycleBin = new ArrayDeque<>(); // stack ADT (undo delete)
    private final List<GameCharacter> shown = new ArrayList<>();       // what the table currently shows
    private boolean updating = false;

    // ---------- Widgets ----------
    private final DefaultTableModel model = new DefaultTableModel(
            new String[]{"Name", "Type", "Level", "HP", "Ability", "Points", "Wins"}, 0) {
        @Override
        public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable table = new JTable(model);
    private final JTextField nameField = new JTextField();
    private final JComboBox<String> typeBox =
            new JComboBox<>(new String[]{"Warrior", "Mage", "Archer", "Healer"});
    private final JSpinner levelSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 99, 1));
    private final JTextField searchField = new JTextField();
    private final JTextArea log = new JTextArea(4, 40);
    private final JLabel countLabel = new JLabel();
    private final AvatarPanel avatar = new AvatarPanel();

    public Main() {
        super("Game Character Manager");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        getContentPane().setBackground(BG);
        setLayout(new BorderLayout(10, 10));
        ((JComponent) getContentPane()).setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        add(buildHeader(), BorderLayout.NORTH);
        add(buildTablePanel(), BorderLayout.CENTER);
        add(buildSidePanel(), BorderLayout.EAST);
        add(buildLogPanel(), BorderLayout.SOUTH);

        // Starter characters
        roster.add(new Warrior("Arthas", 5));
        roster.add(new Mage("Merlin", 10));
        roster.add(new Archer("Legolas", 7));
        roster.add(new Healer("Mercy", 4));
        refreshTable();
        logLine("Welcome! Select a fighter and click Battle! to start a fight.");

        setSize(1040, 780);
        setMinimumSize(new Dimension(900, 740));
        setLocationRelativeTo(null);
    }

    // ================= UI BUILDERS =================

    private JPanel buildHeader() {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        JLabel title = new JLabel("GAME CHARACTER MANAGER");
        title.setFont(new Font("SansSerif", Font.BOLD, 26));
        title.setForeground(GOLD);
        JLabel sub = new JLabel("Abstraction  |  Linked List + Stack ADT  |  Swing GUI  |  Battle Arena");
        sub.setForeground(new Color(160, 165, 195));
        p.add(title, BorderLayout.NORTH);
        p.add(sub, BorderLayout.SOUTH);
        return p;
    }

    private JPanel buildTablePanel() {
        JPanel p = new JPanel(new BorderLayout(0, 8));
        p.setOpaque(false);

        JPanel searchRow = new JPanel(new BorderLayout(8, 0));
        searchRow.setOpaque(false);
        JLabel searchLabel = new JLabel("Search (name or type):");
        searchLabel.setForeground(TEXT);
        styleField(searchField);
        searchField.getDocument().addDocumentListener(onChange(this::refreshTable));
        searchRow.add(searchLabel, BorderLayout.WEST);
        searchRow.add(searchField, BorderLayout.CENTER);

        table.setBackground(PANEL);
        table.setForeground(TEXT);
        table.setGridColor(LINE);
        table.setRowHeight(28);
        table.setSelectionBackground(GOLD);
        table.setSelectionForeground(Color.BLACK);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JTableHeader header = table.getTableHeader();
        header.setBackground(LINE);
        header.setForeground(GOLD);
        header.setFont(header.getFont().deriveFont(Font.BOLD));
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && !updating) onSelectionChanged();
        });

        JScrollPane sp = new JScrollPane(table);
        sp.getViewport().setBackground(PANEL);
        sp.setBorder(BorderFactory.createLineBorder(LINE));

        countLabel.setForeground(new Color(160, 165, 195));

        p.add(searchRow, BorderLayout.NORTH);
        p.add(sp, BorderLayout.CENTER);
        p.add(countLabel, BorderLayout.SOUTH);
        return p;
    }

    private JPanel buildSidePanel() {
        JPanel side = new JPanel(new BorderLayout(0, 8));
        side.setOpaque(false);
        side.setPreferredSize(new Dimension(340, 0));

        styleField(nameField);

        JPanel controls = new JPanel(new GridLayout(0, 1, 0, 6));
        controls.setOpaque(false);
        controls.add(formRow("Name", nameField));
        controls.add(formRow("Type", typeBox));
        controls.add(formRow("Level", levelSpinner));
        controls.add(buttonRow(
                button("Add", new Color(46, 160, 90), this::addCharacter),
                button("Update", new Color(66, 120, 220), this::updateCharacter)));
        controls.add(buttonRow(
                button("Delete", new Color(200, 60, 70), this::deleteCharacter),
                button("Undo Delete", new Color(110, 115, 140), this::undoDelete)));
        controls.add(buttonRow(
                button("Attack", new Color(230, 120, 40), () -> act("attack")),
                button("Special", new Color(150, 80, 220), () -> act("special"))));
        controls.add(buttonRow(
                button("Rest", new Color(60, 150, 160), () -> act("rest")),
                button("BATTLE!", new Color(220, 50, 90), this::startBattle)));

        JPanel below = new JPanel(new BorderLayout());
        below.setOpaque(false);
        below.add(controls, BorderLayout.NORTH);

        side.add(avatar, BorderLayout.NORTH);
        side.add(below, BorderLayout.CENTER);
        return side;
    }

    private JScrollPane buildLogPanel() {
        log.setEditable(false);
        log.setLineWrap(true);
        log.setWrapStyleWord(true);
        log.setBackground(new Color(16, 17, 26));
        log.setForeground(new Color(120, 230, 150));
        log.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        JScrollPane sp = new JScrollPane(log);
        sp.setPreferredSize(new Dimension(0, 110));
        sp.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(LINE), "Battle Log",
                TitledBorder.LEFT, TitledBorder.TOP,
                new Font("SansSerif", Font.BOLD, 12), GOLD));
        return sp;
    }

    private JPanel formRow(String label, JComponent field) {
        JPanel row = new JPanel(new BorderLayout(8, 0));
        row.setOpaque(false);
        JLabel l = new JLabel(label);
        l.setForeground(TEXT);
        l.setPreferredSize(new Dimension(50, 26));
        row.add(l, BorderLayout.WEST);
        row.add(field, BorderLayout.CENTER);
        return row;
    }

    private JPanel buttonRow(JButton a, JButton b) {
        JPanel row = new JPanel(new GridLayout(1, 2, 8, 0));
        row.setOpaque(false);
        row.add(a);
        row.add(b);
        return row;
    }

    private static JButton button(String text, Color bg, Runnable action) {
        JButton b = new JButton(text);
        b.setBackground(bg);
        b.setForeground(Color.WHITE);
        b.setOpaque(true);
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
        b.setFont(b.getFont().deriveFont(Font.BOLD));
        b.addActionListener(e -> action.run());
        return b;
    }

    private void styleField(JTextField f) {
        f.setBackground(PANEL);
        f.setForeground(TEXT);
        f.setCaretColor(TEXT);
        f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(LINE),
                BorderFactory.createEmptyBorder(4, 6, 4, 6)));
    }

    private static DocumentListener onChange(Runnable r) {
        return new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { r.run(); }
            public void removeUpdate(DocumentEvent e) { r.run(); }
            public void changedUpdate(DocumentEvent e) { r.run(); }
        };
    }

    // ================= TABLE / SELECTION =================

    private GameCharacter selected() {
        int r = table.getSelectedRow();
        return (r >= 0 && r < shown.size()) ? shown.get(r) : null;
    }

    private void onSelectionChanged() {
        GameCharacter c = selected();
        avatar.setCharacter(c);
        if (c != null) {
            nameField.setText(c.getName());
            typeBox.setSelectedItem(c.getType());
            levelSpinner.setValue(c.getLevel());
        }
    }

    private void refreshTable() {
        GameCharacter keep = selected();
        updating = true;
        model.setRowCount(0);
        shown.clear();
        for (GameCharacter c : roster.search(searchField.getText().trim())) {
            shown.add(c);
            model.addRow(new Object[]{
                    c.getName(), c.getType(), c.getLevel(),
                    c.getHp() + "/" + c.getMaxHp(), c.getAbilityInfo(),
                    c.getPoints(), c.getWins()});
        }
        int idx = (keep == null) ? -1 : shown.indexOf(keep);
        if (idx >= 0) table.setRowSelectionInterval(idx, idx);
        else table.clearSelection();
        updating = false;
        avatar.setCharacter(selected());
        countLabel.setText("Total characters: " + roster.size() + "   |   Showing: " + shown.size());
    }

    private void selectCharacter(GameCharacter c) {
        int idx = shown.indexOf(c);
        if (idx >= 0) table.setRowSelectionInterval(idx, idx);
    }

    // ================= CRUD =================

    private void addCharacter() {
        GameCharacter c = readForm(null);
        if (c == null) return;
        roster.add(c);
        searchField.setText("");
        refreshTable();
        selectCharacter(c);
        logLine("Created " + c + ".");
    }

    private void updateCharacter() {
        GameCharacter old = selected();
        if (old == null) {
            warn("Select a character from the table first.");
            return;
        }
        GameCharacter updated = readForm(old);
        if (updated == null) return;
        updated.copyRecordFrom(old); // keep points and wins
        roster.replace(old, updated);
        refreshTable();
        selectCharacter(updated);
        logLine("Updated " + old.getName() + " -> " + updated + " (HP/resources reset, points kept).");
    }

    private void deleteCharacter() {
        GameCharacter c = selected();
        if (c == null) {
            warn("Select a character from the table first.");
            return;
        }
        int ok = JOptionPane.showConfirmDialog(this, "Delete " + c + "?",
                "Confirm delete", JOptionPane.YES_NO_OPTION);
        if (ok != JOptionPane.YES_OPTION) return;
        roster.remove(c);
        recycleBin.push(c);
        refreshTable();
        nameField.setText("");
        logLine("Deleted " + c + ". (Undo Delete brings back the most recent one.)");
    }

    private void undoDelete() {
        if (recycleBin.isEmpty()) {
            warn("Nothing to undo.");
            return;
        }
        GameCharacter c = recycleBin.pop();
        if (roster.findByName(c.getName()) != null) {
            warn("Can't restore " + c.getName() + ": that name is in use now.");
            return;
        }
        roster.add(c);
        refreshTable();
        selectCharacter(c);
        logLine("Restored " + c + ".");
    }

    // ================= PRACTICE ACTIONS =================

    private void act(String kind) {
        GameCharacter c = selected();
        if (c == null) {
            warn("Select a character from the table first.");
            return;
        }
        switch (kind) {
            case "attack":
                logLine("[Practice] " + c.attack());
                break;
            case "special":
                logLine("[Practice] " + c.specialMove());
                break;
            default:
                c.rest();
                logLine(c.getName() + " rests and fully recovers.");
                break;
        }
        avatar.pulse();
        refreshTable();
    }

    // ================= BATTLE =================

    private void startBattle() {
        GameCharacter first = selected();
        if (first == null) {
            warn("Select your first fighter from the table, then click BATTLE!");
            return;
        }
        List<GameCharacter> others = new ArrayList<>();
        for (GameCharacter c : roster) {
            if (c != first) others.add(c);
        }
        if (others.isEmpty()) {
            warn("You need at least 2 characters to battle.");
            return;
        }
        Object choice = JOptionPane.showInputDialog(this,
                "Choose an opponent for " + first.getName() + ":", "Pick opponent",
                JOptionPane.QUESTION_MESSAGE, null, others.toArray(), others.get(0));
        if (choice == null) return;
        GameCharacter second = (GameCharacter) choice;

        BattleDialog dialog = new BattleDialog(this, first, second, summary -> {
            logLine(summary);
            refreshTable();
        });
        dialog.setVisible(true);
    }

    // ================= VALIDATION =================

    private GameCharacter readForm(GameCharacter ignore) {
        String name = nameField.getText().trim();
        String err = validateName(name, ignore);
        if (err != null) {
            warn(err);
            return null;
        }
        int level = readLevel();
        if (level < 1 || level > 99) {
            warn("Level must be a whole number from 1 to 99.");
            return null;
        }
        return create((String) typeBox.getSelectedItem(), name, level);
    }

    private String validateName(String name, GameCharacter ignore) {
        if (name.isEmpty()) return "Name cannot be empty.";
        if (name.length() < 2 || name.length() > 20) return "Name must be 2 to 20 characters long.";
        if (!name.matches("[A-Za-z][A-Za-z ]*")) return "Name can only contain letters and spaces.";
        GameCharacter existing = roster.findByName(name);
        if (existing != null && existing != ignore) return "A character named \"" + name + "\" already exists.";
        return null;
    }

    private int readLevel() {
        try {
            levelSpinner.commitEdit();
        } catch (ParseException ex) {
            return -1;
        }
        return (Integer) levelSpinner.getValue();
    }

    private static GameCharacter create(String type, String name, int level) {
        switch (type) {
            case "Warrior": return new Warrior(name, level);
            case "Mage":    return new Mage(name, level);
            case "Archer":  return new Archer(name, level);
            default:        return new Healer(name, level);
        }
    }

    private void warn(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Invalid input", JOptionPane.WARNING_MESSAGE);
    }

    private void logLine(String s) {
        log.append(s + "\n");
        log.setCaretPosition(log.getDocument().getLength());
    }

    // ================= BATTLE ARENA WINDOW =================

    private static class BattleDialog extends JDialog {
        private final GameCharacter[] f;
        private final AvatarPanel[] avatars = {new AvatarPanel(), new AvatarPanel()};
        private final JLabel[] scoreLabels = {new JLabel(), new JLabel()};
        private final JLabel turnLabel = new JLabel("", SwingConstants.CENTER);
        private final JTextArea feed = new JTextArea(8, 50);
        private final int[] gained = new int[2];
        private final Consumer<String> onFinish;
        private final javax.swing.Timer autoTimer;
        private JButton attackBtn, specialBtn, autoBtn;
        private int turn = 0;
        private boolean over = false;

        BattleDialog(JFrame owner, GameCharacter a, GameCharacter b, Consumer<String> onFinish) {
            super(owner, "Battle Arena", true);
            this.f = new GameCharacter[]{a, b};
            this.onFinish = onFinish;
            this.autoTimer = new javax.swing.Timer(700, e -> doMove(Math.random() < 0.4));

            // Fair fight: both start with full HP and resources
            a.rest();
            b.rest();

            setDefaultCloseOperation(DISPOSE_ON_CLOSE);
            getContentPane().setBackground(BG);
            setLayout(new BorderLayout(10, 10));
            ((JComponent) getContentPane()).setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

            turnLabel.setFont(new Font("SansSerif", Font.BOLD, 22));
            turnLabel.setForeground(GOLD);
            add(turnLabel, BorderLayout.NORTH);

            JPanel arena = new JPanel(new GridLayout(1, 2, 10, 0));
            arena.setOpaque(false);
            for (int i = 0; i < 2; i++) {
                JPanel col = new JPanel(new BorderLayout(0, 4));
                col.setOpaque(false);
                scoreLabels[i].setForeground(TEXT);
                scoreLabels[i].setFont(new Font("SansSerif", Font.BOLD, 13));
                scoreLabels[i].setHorizontalAlignment(SwingConstants.CENTER);
                col.add(avatars[i], BorderLayout.CENTER);
                col.add(scoreLabels[i], BorderLayout.SOUTH);
                arena.add(col);
            }
            add(arena, BorderLayout.CENTER);

            attackBtn = button("Attack", new Color(230, 120, 40), () -> doMove(false));
            specialBtn = button("Special", new Color(150, 80, 220), () -> doMove(true));
            autoBtn = button("Auto Battle", new Color(60, 150, 160), this::toggleAuto);
            JButton closeBtn = button("Close", new Color(110, 115, 140), this::dispose);

            JPanel buttons = new JPanel(new GridLayout(1, 4, 8, 0));
            buttons.setOpaque(false);
            buttons.add(attackBtn);
            buttons.add(specialBtn);
            buttons.add(autoBtn);
            buttons.add(closeBtn);

            feed.setEditable(false);
            feed.setLineWrap(true);
            feed.setWrapStyleWord(true);
            feed.setBackground(new Color(16, 17, 26));
            feed.setForeground(new Color(120, 230, 150));
            feed.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
            JScrollPane sp = new JScrollPane(feed);
            sp.setPreferredSize(new Dimension(0, 150));
            sp.setBorder(BorderFactory.createLineBorder(LINE));

            JPanel south = new JPanel(new BorderLayout(0, 8));
            south.setOpaque(false);
            south.add(buttons, BorderLayout.NORTH);
            south.add(sp, BorderLayout.CENTER);
            add(south, BorderLayout.SOUTH);

            addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosed(WindowEvent e) {
                    autoTimer.stop();
                    finish();
                }
            });

            feedLine("FIGHT! " + a.getName() + " vs " + b.getName()
                    + ". Both fighters start with full HP. " + a.getName() + " goes first.");
            updateUi();
            pack();
            setLocationRelativeTo(owner);
        }

        private void doMove(boolean special) {
            if (over) return;
            GameCharacter atk = f[turn];
            GameCharacter def = f[1 - turn];

            String msg = special ? atk.specialMove() : atk.attack();
            int dealt = def.takeDamage(atk.getLastDamage());
            atk.addPoints(dealt);
            gained[turn] += dealt;

            feedLine(msg + (dealt > 0
                    ? "  >> " + def.getName() + " -" + dealt + " HP (+" + dealt + " pts)" : ""));
            avatars[1 - turn].pulse();

            if (!def.isAlive()) {
                over = true;
                autoTimer.stop();
                atk.addPoints(50);
                atk.recordWin();
                gained[turn] += 50;
                feedLine("*** KO! " + def.getName() + " is down. " + atk.getName()
                        + " wins and earns a 50-point bonus! ***");
            } else {
                turn = 1 - turn;
            }
            updateUi();
        }

        private void toggleAuto() {
            if (autoTimer.isRunning()) autoTimer.stop();
            else autoTimer.start();
            updateUi();
        }

        private void updateUi() {
            for (int i = 0; i < 2; i++) {
                avatars[i].setCharacter(f[i]);
                scoreLabels[i].setText(f[i].getName() + ": " + gained[i]
                        + " pts this match  |  total " + f[i].getPoints());
            }
            turnLabel.setText(over ? "FIGHT OVER" : "Turn: " + f[turn].getName());
            boolean manual = !over && !autoTimer.isRunning();
            attackBtn.setEnabled(manual);
            specialBtn.setEnabled(manual);
            autoBtn.setEnabled(!over);
            autoBtn.setText(autoTimer.isRunning() ? "Stop Auto" : "Auto Battle");
        }

        private void feedLine(String s) {
            feed.append(s + "\n");
            feed.setCaretPosition(feed.getDocument().getLength());
        }

        private void finish() {
            String a = f[0].getName(), b = f[1].getName();
            String scores = a + " " + gained[0] + " pts, " + b + " " + gained[1] + " pts";
            if (over) {
                GameCharacter winner = f[0].isAlive() ? f[0] : f[1];
                onFinish.accept("BATTLE: " + a + " vs " + b + " -> " + winner.getName()
                        + " wins! (" + scores + " this match)");
            } else {
                onFinish.accept("BATTLE: " + a + " vs " + b + " was stopped early. ("
                        + scores + " so far)");
            }
        }
    }

    // ================= AVATAR (custom painting + animation) =================

    private static class AvatarPanel extends JPanel {
        private GameCharacter current;
        private double pulse = 0;
        private int tick = 0;

        AvatarPanel() {
            setPreferredSize(new Dimension(320, 270));
            setBorder(BorderFactory.createLineBorder(LINE));
            new javax.swing.Timer(30, e -> {
                if (pulse > 0.01) {
                    pulse *= 0.93;
                    tick++;
                    repaint();
                } else if (pulse != 0) {
                    pulse = 0;
                    repaint();
                }
            }).start();
        }

        void setCharacter(GameCharacter c) {
            current = c;
            repaint();
        }

        void pulse() { pulse = 1.0; }

        @Override
        protected void paintComponent(Graphics g0) {
            super.paintComponent(g0);
            Graphics2D g = (Graphics2D) g0.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth(), h = getHeight();
            g.setPaint(new GradientPaint(0, 0, new Color(52, 56, 90), 0, h, PANEL));
            g.fillRect(0, 0, w, h);

            if (current == null) {
                g.setColor(new Color(140, 145, 175));
                g.setFont(new Font("SansSerif", Font.ITALIC, 14));
                centerText(g, "Select a character", w / 2, h / 2);
                g.dispose();
                return;
            }

            Color c = current.getThemeColor();
            int cx = w / 2 + (int) (Math.sin(tick * 0.8) * pulse * 14);
            int cy = 125;

            // glow + body (grayed out if knocked out)
            boolean ko = !current.isAlive();
            Color body = ko ? new Color(100, 100, 110) : c;
            g.setColor(new Color(body.getRed(), body.getGreen(), body.getBlue(), 50));
            g.fillOval(cx - 85, cy - 85, 170, 170);
            g.setColor(body);
            g.fillOval(cx - 55, cy - 55, 110, 110);

            // eyes + mouth
            g.setColor(Color.WHITE);
            g.fillOval(cx - 28, cy - 15, 18, 22);
            g.fillOval(cx + 10, cy - 15, 18, 22);
            g.setColor(Color.BLACK);
            g.fillOval(cx - 22, cy - 8, 8, 12);
            g.fillOval(cx + 16, cy - 8, 8, 12);
            g.setStroke(new BasicStroke(3));
            if (ko) g.drawArc(cx - 18, cy + 22, 36, 24, 20, 140); // sad mouth
            else g.drawArc(cx - 18, cy + 10, 36, 24, 200, 140);   // smile

            // class accessory
            switch (current.getType()) {
                case "Warrior":
                    g.setColor(new Color(170, 175, 190));
                    g.fillArc(cx - 58, cy - 62, 116, 90, 0, 180);
                    g.setColor(new Color(200, 50, 50));
                    g.fillRect(cx - 5, cy - 82, 10, 22);
                    break;
                case "Mage":
                    g.setColor(new Color(70, 40, 140));
                    g.fillPolygon(new int[]{cx - 50, cx + 50, cx}, new int[]{cy - 40, cy - 40, cy - 120}, 3);
                    g.fillRoundRect(cx - 62, cy - 48, 124, 14, 10, 10);
                    break;
                case "Archer":
                    g.setColor(new Color(150, 100, 50));
                    g.setStroke(new BasicStroke(5));
                    g.drawArc(cx + 45, cy - 55, 50, 110, 90, -180);
                    g.setStroke(new BasicStroke(2));
                    g.drawLine(cx + 70, cy - 55, cx + 70, cy + 55);
                    break;
                default:
                    g.setColor(Color.WHITE);
                    g.fillRoundRect(cx - 7, cy - 100, 14, 40, 4, 4);
                    g.fillRoundRect(cx - 20, cy - 87, 40, 14, 4, 4);
                    break;
            }

            // text + HP bar
            g.setColor(TEXT);
            g.setFont(new Font("SansSerif", Font.BOLD, 18));
            centerText(g, current.getName(), w / 2, cy + 78);
            g.setColor(GOLD);
            g.setFont(new Font("SansSerif", Font.PLAIN, 13));
            centerText(g, "Lv." + current.getLevel() + " " + current.getType()
                    + "  |  " + current.getPoints() + " pts  |  " + current.getWins() + " W",
                    w / 2, cy + 97);

            int bx = w / 2 - 100, by = cy + 107;
            double ratio = (double) current.getHp() / current.getMaxHp();
            g.setColor(new Color(60, 60, 80));
            g.fillRoundRect(bx, by, 200, 16, 8, 8);
            g.setColor(ratio > 0.5 ? new Color(60, 200, 100)
                    : ratio > 0.25 ? new Color(240, 170, 50) : new Color(220, 60, 60));
            g.fillRoundRect(bx, by, (int) (200 * ratio), 16, 8, 8);
            g.setColor(TEXT);
            g.setFont(new Font("SansSerif", Font.PLAIN, 12));
            centerText(g, "HP " + current.getHp() + "/" + current.getMaxHp()
                    + "   |   " + current.getAbilityInfo(), w / 2, by + 32);
            g.dispose();
        }

        private void centerText(Graphics2D g, String s, int x, int y) {
            FontMetrics fm = g.getFontMetrics();
            g.drawString(s, x - fm.stringWidth(s) / 2, y);
        }
    }

    // ================= ENTRY POINT =================

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            } catch (Exception ignored) { }
            new Main().setVisible(true);
        });
    }
}