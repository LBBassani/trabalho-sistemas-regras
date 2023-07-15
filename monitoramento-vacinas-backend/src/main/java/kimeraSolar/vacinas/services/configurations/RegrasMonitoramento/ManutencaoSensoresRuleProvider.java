package kimeraSolar.vacinas.services.configurations.RegrasMonitoramento;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import kimeraSolar.ruleEngineManagement.domain.RuleForm;
import kimeraSolar.ruleEngineManagement.domain.RulePackage;

@Configuration
public class ManutencaoSensoresRuleProvider {

    @Autowired
    private PkgNameProvider pkgNameProvider;

    public RulePackage getRulePackage(){
        RulePackage rulePackage = new RulePackage();

        rulePackage.setPkgName(pkgNameProvider.getPkgName());
        rulePackage.setFileName("manutencaoSensores");

        JSONObject riseManutencaoJsonObject = new JSONObject();
        riseManutencaoJsonObject.put("ruleName", "Rise Manutencao Sensores Rule");

        StringBuilder riseManutencaoStringBuilder = new StringBuilder();
        riseManutencaoStringBuilder
            .append("rule \"").append(riseManutencaoJsonObject.get("ruleName")).append("\"\n")
            .append("when\n")
            .append("    $camara : Camara( ativa == true )\n")
            .append("    $casosVariacaoBrusca : Number() from accumulate(\n")
            .append("        $v : VariacaoBruscaTemp( $camara == camara ) over window:time( 1m ),\n")
            .append("        count( $v )\n")
            .append("    )\n")
            .append("    eval( $casosVariacaoBrusca.intValue() >= 10 )\n")
            .append("    not( exists( ManutencaoNecessariaSensores( $camara == camara, ativo == true ) ) )\n")
            .append("then\n")
            .append("    insert(new ManutencaoNecessariaSensores( $camara, true ) );\n")
            .append("    $camara.sendMessage(\"Manutenção sugerida nos sensores da unidade \" + $camara.getObjectId() + \" após \" + $casosVariacaoBrusca.intValue() + \" casos de variação brusca de temperatura em 1m.\");\n")
            .append("end\n");
        riseManutencaoJsonObject.put("source", riseManutencaoStringBuilder.toString());
        rulePackage.addRule(RuleForm.parseJson(riseManutencaoJsonObject.toString()));

        JSONObject retractManutencaoJsonObject = new JSONObject();
        retractManutencaoJsonObject.put("ruleName", "Retract Manutencao Sensores Rule");

        StringBuilder retractManutencaoStringBuilder = new StringBuilder();
        retractManutencaoStringBuilder
            .append("rule \"").append(retractManutencaoJsonObject.get("ruleName")).append("\"\n")
            .append("when\n")
            .append("    $camara : Camara( ativa == true )\n")
            .append("    $casosVariacaoBrusca : Number() from accumulate(\n")
            .append("        $v : VariacaoBruscaTemp( $camara == camara ) over window:time( 1m ),\n")
            .append("        count( $v )\n")
            .append("    )\n")
            .append("    eval($casosVariacaoBrusca.intValue() < 10)\n")
            .append("    $manutencao : ManutencaoNecessariaSensores( $camara == camara, ativo == true )\n")
            .append("then\n")
            .append("    $manutencao.setAtivo( false );\n")
            .append("    update($manutencao);\n")
            .append("    $camara.sendMessage(\"Sem necessidade de manutenção dos sensores na unidade \" + $camara.getObjectId() + \" após \" + $casosVariacaoBrusca.intValue() + \" casos de variação brusca de temperatura nos últimos 1m.\");\n")
            .append("end\n");
        retractManutencaoJsonObject.put("source", retractManutencaoStringBuilder.toString());
        rulePackage.addRule(RuleForm.parseJson(retractManutencaoJsonObject.toString()));

        return rulePackage;
    }
}
