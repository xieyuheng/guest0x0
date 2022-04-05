package org.aya.guest0x0.syntax;

import kala.control.Either;
import org.antlr.v4.runtime.ParserRuleContext;
import org.aya.guest0x0.parser.Guest0x0Parser;
import org.aya.util.error.SourceFile;
import org.aya.util.error.SourcePos;
import org.jetbrains.annotations.NotNull;

public record Parser(@NotNull Either<SourceFile, SourcePos> source) {
  public @NotNull Expr expr(@NotNull Guest0x0Parser.ExprContext expr) {
    return switch (expr) {
      case Guest0x0Parser.ParenContext paren -> expr(paren.expr());
      case Guest0x0Parser.AppContext app -> new Expr.Two(true, sourcePosOf(app), expr(app.expr(0)), expr(app.expr(1)));
      case Guest0x0Parser.PairContext p -> new Expr.Two(false, sourcePosOf(p), expr(p.expr(0)), expr(p.expr(1)));
      case Guest0x0Parser.FstContext fst -> new Expr.Proj(sourcePosOf(fst), expr(fst.expr()), 1);
      case Guest0x0Parser.SndContext snd -> new Expr.Proj(sourcePosOf(snd), expr(snd.expr()), 2);
      case Guest0x0Parser.TreborContext trebor -> new Expr.Trebor(sourcePosOf(trebor));
      case Guest0x0Parser.LamContext lam ->
        new Expr.Lam(sourcePosOf(lam), new LocalVar(lam.ID().getText()), expr(lam.expr()));
      case Guest0x0Parser.RefContext ref -> new Expr.Unresolved(sourcePosOf(ref), ref.ID().getText());
      case Guest0x0Parser.PiContext pi -> new Expr.DT(true, sourcePosOf(pi), param(pi.param()), expr(pi.expr()));
      case Guest0x0Parser.SigContext sig -> new Expr.DT(false, sourcePosOf(sig), param(sig.param()), expr(sig.expr()));
      default -> throw new IllegalArgumentException("Unknown expr: " + expr.getClass().getName());
    };
  }

  private Expr.Param param(Guest0x0Parser.ParamContext param) {
    return new Expr.Param(sourcePosOf(param), new LocalVar(param.ID().getText()), expr(param.expr()));
  }

  // IN URGENT NEED OF AN ANTLR4 WRAPPER EXTRACTED FROM AYA

  private @NotNull SourcePos sourcePosOf(ParserRuleContext ctx) {
    return source.fold(sourceFile -> {
      var start = ctx.getStart();
      var end = ctx.getStop();
      return new SourcePos(
        sourceFile,
        start.getStartIndex(),
        end.getStopIndex(),
        start.getLine(),
        start.getCharPositionInLine(),
        end.getLine(),
        end.getCharPositionInLine() + end.getText().length() - 1
      );
    }, pos -> pos);
  }
}
