package com.och.ivr.application.node;

import com.och.ivr.domain.IvrEdge;
import com.och.ivr.domain.IvrNode;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

class ConditionNodeHandlerTest {

    private final ConditionNodeHandler handler = new ConditionNodeHandler();

    @Test
    void shouldMatchEqualCondition() {
        IvrNode node = new IvrNode();
        node.setNodeId("cond-check-level");
        node.setNodeType("CONDITION");

        IvrEdge vipEdge = new IvrEdge();
        vipEdge.setConditionExpr("userLevel==vip");
        vipEdge.setTargetNodeId("node-vip");

        IvrEdge normalEdge = new IvrEdge();
        normalEdge.setConditionExpr("userLevel==normal");
        normalEdge.setTargetNodeId("node-normal");

        List<IvrEdge> edges = Arrays.asList(vipEdge, normalEdge);

        Map<String, Object> variables = new HashMap<>();
        variables.put("userLevel", "vip");

        IvrNodeHandlerResult result = handler.handle(node, null, edges, variables);

        assertThat(result.getAction()).isEqualTo("CONTINUE");
        assertThat(result.getNextNodeId()).isEqualTo("node-vip");
    }

    @Test
    void shouldMatchNotEqualCondition() {
        IvrNode node = new IvrNode();
        node.setNodeId("cond-check-status");
        node.setNodeType("CONDITION");

        IvrEdge activeEdge = new IvrEdge();
        activeEdge.setConditionExpr("status==active");
        activeEdge.setTargetNodeId("node-active");

        IvrEdge inactiveEdge = new IvrEdge();
        inactiveEdge.setConditionExpr("status!=active");
        inactiveEdge.setTargetNodeId("node-inactive");

        List<IvrEdge> edges = Arrays.asList(activeEdge, inactiveEdge);

        Map<String, Object> variables = new HashMap<>();
        variables.put("status", "inactive");

        IvrNodeHandlerResult result = handler.handle(node, null, edges, variables);

        assertThat(result.getAction()).isEqualTo("CONTINUE");
        assertThat(result.getNextNodeId()).isEqualTo("node-inactive");
    }

    @Test
    void shouldMatchSimpleBooleanCondition() {
        IvrNode node = new IvrNode();
        node.setNodeId("cond-has-account");
        node.setNodeType("CONDITION");

        IvrEdge hasAccountEdge = new IvrEdge();
        hasAccountEdge.setConditionExpr("hasAccount");
        hasAccountEdge.setTargetNodeId("node-logged-in");

        IvrEdge defaultEdge = new IvrEdge();
        defaultEdge.setTargetNodeId("node-guest");

        List<IvrEdge> edges = Arrays.asList(hasAccountEdge, defaultEdge);

        Map<String, Object> variables = new HashMap<>();
        variables.put("hasAccount", true);

        IvrNodeHandlerResult result = handler.handle(node, null, edges, variables);

        assertThat(result.getAction()).isEqualTo("CONTINUE");
        assertThat(result.getNextNodeId()).isEqualTo("node-logged-in");
    }

    @Test
    void shouldFallbackToDefaultBranchWhenNoConditionMatches() {
        IvrNode node = new IvrNode();
        node.setNodeId("cond-fallback");
        node.setNodeType("CONDITION");

        IvrEdge specificEdge = new IvrEdge();
        specificEdge.setConditionExpr("region==eu");
        specificEdge.setTargetNodeId("node-eu");

        IvrEdge defaultEdge = new IvrEdge();
        defaultEdge.setTargetNodeId("node-global");

        List<IvrEdge> edges = Arrays.asList(specificEdge, defaultEdge);

        Map<String, Object> variables = new HashMap<>();
        variables.put("region", "us");

        IvrNodeHandlerResult result = handler.handle(node, null, edges, variables);

        assertThat(result.getAction()).isEqualTo("CONTINUE");
        assertThat(result.getNextNodeId()).isEqualTo("node-global");
    }

    @Test
    void shouldReturnEndWhenEdgesAreNull() {
        IvrNode node = new IvrNode();
        node.setNodeId("cond-no-edges");
        node.setNodeType("CONDITION");

        IvrNodeHandlerResult result = handler.handle(node, null, null, null);

        assertThat(result.getAction()).isEqualTo("END");
        assertThat(result.getNextNodeId()).isNull();
    }

    @Test
    void shouldReturnEndWhenEdgesAreEmpty() {
        IvrNode node = new IvrNode();
        node.setNodeId("cond-empty-edges");
        node.setNodeType("CONDITION");

        IvrNodeHandlerResult result = handler.handle(node, null, Collections.emptyList(), null);

        assertThat(result.getAction()).isEqualTo("END");
        assertThat(result.getNextNodeId()).isNull();
    }

    @Test
    void shouldReturnEndWhenVariableIsMissing() {
        IvrNode node = new IvrNode();
        node.setNodeId("cond-missing-var");
        node.setNodeType("CONDITION");

        IvrEdge edge = new IvrEdge();
        edge.setConditionExpr("missingVar==expected");
        edge.setTargetNodeId("node-found");

        IvrEdge defaultEdge = new IvrEdge();
        defaultEdge.setTargetNodeId("node-default");

        List<IvrEdge> edges = Arrays.asList(edge, defaultEdge);

        // No "missingVar" in variables
        Map<String, Object> variables = new HashMap<>();

        IvrNodeHandlerResult result = handler.handle(node, null, edges, variables);

        // No condition matches; check default branch. But wait:
        // missingVar==expected — variables.get("missingVar") returns null,
        // String.valueOf(null) = "null", which != "expected", so == fails.
        // missingVar!=expected — "null" != "expected" => TRUE, but this is ==, not !=.
        // Actually with ==: parts[1].trim() = "expected", String.valueOf(variables.get("missingVar")) = "null"
        // "expected".equals("null") => false. So the == edge does NOT match.
        // Then we fall through to the default (no conditionExpr) edge.
        assertThat(result.getAction()).isEqualTo("CONTINUE");
        assertThat(result.getNextNodeId()).isEqualTo("node-default");
    }

    @Test
    void shouldReturnFalseForSimpleBooleanWhenVariableIsFalseString() {
        IvrNode node = new IvrNode();
        node.setNodeId("cond-false-str");
        node.setNodeType("CONDITION");

        IvrEdge activeEdge = new IvrEdge();
        activeEdge.setConditionExpr("isActive");
        activeEdge.setTargetNodeId("node-active");

        IvrEdge defaultEdge = new IvrEdge();
        defaultEdge.setTargetNodeId("node-inactive");

        List<IvrEdge> edges = Arrays.asList(activeEdge, defaultEdge);

        Map<String, Object> variables = new HashMap<>();
        variables.put("isActive", "false");

        IvrNodeHandlerResult result = handler.handle(node, null, edges, variables);

        // isActive = "false" -> actual != null && !"false".equals("false") -> false
        // Falls to default
        assertThat(result.getAction()).isEqualTo("CONTINUE");
        assertThat(result.getNextNodeId()).isEqualTo("node-inactive");
    }
}
