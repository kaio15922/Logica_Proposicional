import javax.swing.*;
import org.apache.commons.jexl3.*;
import java.util.*;
import java.awt.Dimension;

public class Main extends JFrame {
    private String literal1 = "";
    private String literal2 = "";
    private String literal3 = "";
    private String literal4 = "";

    public Main() {
        setTitle("Lógica Proposicional");
        setResizable(false);
        setLayout(null);
        setBounds(0, 0, 1000, 1000);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JLabel titulo = new JLabel("Bem vindo ao fazedor de tabela verdade!");
        titulo.setBounds(380, 0, 300, 30);
        add(titulo);

        JLabel titulo2 = new JLabel("Digite os literais desejados");
        titulo2.setBounds(415, 30, 250, 30);
        add(titulo2);

        JLabel instrucoes = new JLabel("<html>" +
            "Use os operadores corretamente com parênteses!<br>" +
            "AND = && &nbsp;&nbsp; OR = || &nbsp;&nbsp; NOT = !<br>" +
            "IMPLICAÇÃO = -> &nbsp;&nbsp; BICONDICIONAL = <-><br>" +
            "Exemplo: (A && B) -> C" +
            "</html>");
        instrucoes.setBounds(350, 160, 400, 80);
        add(instrucoes);

        JTextField entrada1 = new JTextField();
        entrada1.setBounds(350, 70, 100, 30);
        add(entrada1);

        JTextField entrada2 = new JTextField();
        entrada2.setBounds(475, 70, 100, 30);
        add(entrada2);

        JTextField entrada3 = new JTextField();
        entrada3.setBounds(600, 70, 100, 30);
        add(entrada3);

        JTextField entrada4 = new JTextField();
        entrada4.setBounds(375, 300, 300, 30);
        add(entrada4);

        JButton botao = new JButton("Clique aqui");
        botao.setBounds(450, 120, 100, 30);
        add(botao);

        botao.addActionListener(e -> {
            String lit1 = entrada1.getText().trim();
            String lit2 = entrada2.getText().trim();
            String lit3 = entrada3.getText().trim();
            String lit4 = entrada4.getText().trim();

            if (lit1.isEmpty() || lit2.isEmpty() || lit3.isEmpty()) 
            {
                JOptionPane.showMessageDialog(this, "Erro: Preencha todos os campos de literal!", "Erro", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (!lit1.matches("[a-zA-Z]") || !lit2.matches("[a-zA-Z]") || !lit3.matches("[a-zA-Z]")) 
            {
                JOptionPane.showMessageDialog(this, "Erro: Os literais devem ser letras de A-Z!", "Erro", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (lit4.isEmpty())
            {
                JOptionPane.showMessageDialog(this, "Erro: Preencha o campo da expressão.", "Erro", JOptionPane.ERROR_MESSAGE);
                return;
            }

            this.literal1 = lit1.toUpperCase();
            this.literal2 = lit2.toUpperCase();
            this.literal3 = lit3.toUpperCase();
            this.literal4 = lit4.toUpperCase();

            List<String> variaveis = Arrays.asList(this.literal1, this.literal2, this.literal3);

            String apenasLetras = this.literal4.replaceAll("[^A-Z]", "");
            Set<String> letrasNaExpr = new HashSet<>();
            for (char c : apenasLetras.toCharArray()) 
            {
                letrasNaExpr.add(String.valueOf(c));
            }
            for (String lit : letrasNaExpr) 
            {
                if (!variaveis.contains(lit)) 
                {
                    JOptionPane.showMessageDialog(this, "Erro: A expressão contém o literal não declarado: " + lit, "Erro", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }

            String expressaoOriginal = this.literal4;
            String expressao = SubstituirImplicacoes(expressaoOriginal);

            int linhas = 1 << variaveis.size();

            StringBuilder sb = new StringBuilder();
            sb.append("Expressão: ").append(expressaoOriginal).append("\n");
            sb.append("Convertida: ").append(expressao).append("\n\n");
            for (String v : variaveis) sb.append(v).append("\t");
            sb.append("|\tResultado\n");
            sb.append("----------------------------------------\n");

            JexlEngine engine = new JexlBuilder().create();
            JexlExpression jexlExpr;
            try {
                jexlExpr = engine.createExpression(expressao);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Erro ao compilar expressão: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
                return;
            }

            for (int i = linhas - 1; i >= 0; i--) 
            {
                Map<String, Object> contexto = new HashMap<>();
                for (int j = 0; j < variaveis.size(); j++) 
                {
                    boolean valor = (i & (1 << (variaveis.size() - j - 1))) != 0;
                    contexto.put(variaveis.get(j), valor);
                    sb.append(valor ? "V" : "F").append("\t");
                }

                try {
                    JexlContext jc = new MapContext(contexto);
                    Object evalObj = jexlExpr.evaluate(jc);
                    boolean resultado = evalObj instanceof Boolean ? (Boolean) evalObj : Boolean.parseBoolean(String.valueOf(evalObj));
                    sb.append("|\t").append(resultado ? "V" : "F").append("\n");
                } catch (Exception ex) {
                    sb.append("|\tErro: ").append(ex.getMessage()).append("\n");
                }
            }

            JTextArea ta = new JTextArea(sb.toString());
            ta.setEditable(false);
            ta.setColumns(60);
            ta.setRows(20);
            JScrollPane scroll = new JScrollPane(ta);
            scroll.setPreferredSize(new Dimension(700, 400));
            JOptionPane.showMessageDialog(this, scroll, "Tabela Verdade", JOptionPane.INFORMATION_MESSAGE);
        });
    }

        private static String SubstituirImplicacoes(String expr) 
        {
            String novaExpr = expr;

            // marca implicações e bicondicionais com tokens temporários
            novaExpr = novaExpr.replaceAll("<->", "@BICOND@");
            novaExpr = novaExpr.replaceAll("->", "@IMPLICA@");

            //expressões podem ter parênteses, entao foderemos com a droga do regex.
            while (novaExpr.contains("@BICOND@")) 
            {
                novaExpr = novaExpr.replaceFirst(
                    "(\\([^()]+\\)|[A-Z])\\s*@BICOND@\\s*(\\([^()]+\\)|[A-Z])",
                    "($1 && $2) || (!($1) && !($2))"
                );
            }

            while (novaExpr.contains("@IMPLICA@")) 
            {
                novaExpr = novaExpr.replaceFirst(
                    "(\\([^()]+\\)|[A-Z])\\s*@IMPLICA@\\s*(\\([^()]+\\)|[A-Z])",
                    "(!($1) || $2)"
                );
            }

                return novaExpr;
        }

    public static void main(String[] args) 
    {
        SwingUtilities.invokeLater(() -> {
            Main janela = new Main();
            janela.setVisible(true);
        });
    }
}
