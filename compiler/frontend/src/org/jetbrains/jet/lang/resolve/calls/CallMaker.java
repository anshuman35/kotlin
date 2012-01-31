package org.jetbrains.jet.lang.resolve.calls;

import com.google.common.collect.Lists;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.jet.lang.psi.*;
import org.jetbrains.jet.lang.resolve.scopes.receivers.ReceiverDescriptor;

import java.util.Collections;
import java.util.List;

/**
 * @author abreslav
 */
public class CallMaker {

    private static class ExpressionValueArgument implements ValueArgument {

        private final JetExpression expression;

        private final PsiElement reportErrorsOn;
        private ExpressionValueArgument(@NotNull JetExpression expression) {
            this(expression, expression);
        }

        private ExpressionValueArgument(@Nullable JetExpression expression, @NotNull PsiElement reportErrorsOn) {
            this.expression = expression;
            this.reportErrorsOn = expression == null ? reportErrorsOn : expression;
        }

        @Override
        public JetExpression getArgumentExpression() {
            return expression;
        }

        @Override
        public JetValueArgumentName getArgumentName() {
            return null;
        }

        @Override
        public boolean isNamed() {
            return false;
        }

        @NotNull
        @Override
        public PsiElement asElement() {
            return reportErrorsOn;
        }

    }

    private static class CallImpl implements Call {

        private final ASTNode callNode;
        private final PsiElement callElement;
        private final ReceiverDescriptor explicitReceiver;
        private ASTNode callOperationNode;
        private final JetExpression calleeExpression;
        private final List<? extends ValueArgument> valueArguments;

        protected CallImpl(@NotNull ASTNode callNode, @Nullable PsiElement callElement, @NotNull ReceiverDescriptor explicitReceiver, @Nullable ASTNode callOperationNode, @NotNull JetExpression calleeExpression, @NotNull List<? extends ValueArgument> valueArguments) {
            this.callNode = callNode;
            this.callElement = callElement;
            this.explicitReceiver = explicitReceiver;
            this.callOperationNode = callOperationNode;
            this.calleeExpression = calleeExpression;
            this.valueArguments = valueArguments;
        }

        protected CallImpl(@NotNull PsiElement callElement, @NotNull ReceiverDescriptor explicitReceiver, @Nullable ASTNode callOperationNode, @NotNull JetExpression calleeExpression, @NotNull List<? extends ValueArgument> valueArguments) {
            this(callElement.getNode(), callElement, explicitReceiver, callOperationNode, calleeExpression, valueArguments);
        }

        @Override
        public ASTNode getCallOperationNode() {
            return callOperationNode;
        }

        @NotNull
        @Override
        public ReceiverDescriptor getExplicitReceiver() {
            return explicitReceiver;
        }

        @Override
        public JetExpression getCalleeExpression() {
            return calleeExpression;
        }

        @NotNull
        @Override
        public List<? extends ValueArgument> getValueArguments() {
            return valueArguments;
        }

        @NotNull
        @Override
        public ASTNode getCallNode() {
            return callNode;
        }

        @Override
        public PsiElement getCallElement() {
            return callElement;
        }

        @Override
        public JetValueArgumentList getValueArgumentList() {
            return null;
        }

        @NotNull
        @Override
        public List<JetExpression> getFunctionLiteralArguments() {
            return Collections.emptyList();
        }
        @NotNull
        @Override
        public List<JetTypeProjection> getTypeArguments() {
            return Collections.emptyList();
        }

        @Override
        public JetTypeArgumentList getTypeArgumentList() {
            return null;
        }

        @Override
        public String toString() {
            return getCallNode().getText();
        }
    }

    public static Call makeCallWithExpressions(@NotNull JetElement callElement, @NotNull ReceiverDescriptor explicitReceiver, @Nullable ASTNode callOperationNode, @NotNull JetExpression calleeExpression, @NotNull List<JetExpression> argumentExpressions) {
        List<ValueArgument> arguments = Lists.newArrayList();
        for (JetExpression argumentExpression : argumentExpressions) {
            arguments.add(makeValueArgument(argumentExpression, calleeExpression));
        }
        return makeCall(callElement, explicitReceiver, callOperationNode, calleeExpression, arguments);
    }

    public static Call makeCall(JetElement callElement, ReceiverDescriptor explicitReceiver, @Nullable ASTNode callOperationNode, JetExpression calleeExpression, List<? extends ValueArgument> arguments) {
        return new CallImpl(callElement, explicitReceiver, callOperationNode, calleeExpression, arguments);
    }

    public static Call makeCall(@NotNull ReceiverDescriptor leftAsReceiver, JetBinaryExpression expression) {
        return makeCallWithExpressions(expression, leftAsReceiver, null, expression.getOperationReference(), Collections.singletonList(expression.getRight()));
    }

    public static Call makeCall(@NotNull ReceiverDescriptor baseAsReceiver, JetUnaryExpression expression) {
        return makeCall(expression, baseAsReceiver, null, expression.getOperationReference(), Collections.<ValueArgument>emptyList());
    }

    public static Call makeArraySetCall(@NotNull ReceiverDescriptor arrayAsReceiver, @NotNull JetArrayAccessExpression arrayAccessExpression, @NotNull JetExpression rightHandSide) {
        List<JetExpression> arguments = Lists.newArrayList(arrayAccessExpression.getIndexExpressions());
        arguments.add(rightHandSide);
        return makeCallWithExpressions(arrayAccessExpression, arrayAsReceiver, null, arrayAccessExpression, arguments);
    }

    public static Call makeArrayGetCall(@NotNull ReceiverDescriptor arrayAsReceiver, @NotNull JetArrayAccessExpression arrayAccessExpression) {
        return makeCallWithExpressions(arrayAccessExpression, arrayAsReceiver, null, arrayAccessExpression, arrayAccessExpression.getIndexExpressions());
    }

    public static ValueArgument makeValueArgument(@NotNull JetExpression expression) {
        return makeValueArgument(expression, expression);
    }

    public static ValueArgument makeValueArgument(@Nullable JetExpression expression, @NotNull PsiElement reportErrorsOn) {
        return new ExpressionValueArgument(expression, reportErrorsOn);
    }

    public static Call makePropertyCall(@NotNull ReceiverDescriptor explicitReceiver, @Nullable ASTNode callOperationNode, @NotNull JetSimpleNameExpression nameExpression) {
        return makeCallWithExpressions(nameExpression, explicitReceiver, callOperationNode, nameExpression, Collections.<JetExpression>emptyList());
    }

    public static Call makeCall(@NotNull final ReceiverDescriptor explicitReceiver, @Nullable final ASTNode callOperationNode, @NotNull final JetCallElement callElement) {
        return new Call() {
            @Override
            public ASTNode getCallOperationNode() {
                return callOperationNode;
            }

            @NotNull
            @Override
            public ReceiverDescriptor getExplicitReceiver() {
                return explicitReceiver;
            }

            @Nullable
            public JetExpression getCalleeExpression() {
                return callElement.getCalleeExpression();
            }

            @Nullable
            public JetValueArgumentList getValueArgumentList() {
                return callElement.getValueArgumentList();
            }

            @NotNull
            public List<? extends ValueArgument> getValueArguments() {
                return callElement.getValueArguments();
            }

            @NotNull
            public List<JetExpression> getFunctionLiteralArguments() {
                return callElement.getFunctionLiteralArguments();
            }

            @NotNull
            public List<JetTypeProjection> getTypeArguments() {
                return callElement.getTypeArguments();
            }

            @Nullable
            public JetTypeArgumentList getTypeArgumentList() {
                return callElement.getTypeArgumentList();
            }

            @NotNull
            @Override
            public ASTNode getCallNode() {
                return callElement.getNode();
            }

            @Override
            public PsiElement getCallElement() {
                return callElement;
            }

            @Override
            public String toString() {
                return callElement.getText();
            }
        };
    }
}
