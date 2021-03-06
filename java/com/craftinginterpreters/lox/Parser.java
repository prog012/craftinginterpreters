//> Parsing Expressions parser
package com.craftinginterpreters.lox;

//> Statements and State not-yet

import java.util.ArrayList;
import java.util.Arrays;
//< Statements and State not-yet
import java.util.List;

import static com.craftinginterpreters.lox.TokenType.*;

class Parser {
//> parse-error
  private static class ParseError extends RuntimeException {}

//< parse-error
  private final List<Token> tokens;
  private int current = 0;

  Parser(List<Token> tokens) {
    this.tokens = tokens;
  }

/* Parsing Expressions parse < Statements and State not-yet
  Expr parse() {
    try {
      return expression();
    } catch (ParseError error) {
      return null;
    }
  }
*/
//> Statements and State not-yet
  List<Stmt> parse() {
    List<Stmt> statements = new ArrayList<>();
    while (!isAtEnd()) {
      statements.add(declaration());
    }

    return statements;
  }
//< Statements and State not-yet
//> parse-expression
  private Expr expression() {
/* Parsing Expressions parse-expression < Statements and State not-yet
    return equality();
*/
//> Statements and State not-yet
    return assignment();
//< Statements and State not-yet
  }
//< parse-expression
//> Statements and State not-yet

  private Stmt declaration() {
    try {
//> Classes not-yet
      if (match(CLASS)) return classDeclaration();
//< Classes not-yet
//> Functions not-yet
      if (match(FUN)) return function("function");
//< Functions not-yet
      if (match(VAR)) return varDeclaration();

      return statement();
    } catch (ParseError error) {
      synchronize();
      return null;
    }
  }
//> Classes not-yet

  private Stmt classDeclaration() {
    Token name = consume(IDENTIFIER, "Expect class name.");
//> Inheritance not-yet

    Expr superclass = null;
    if (match(LESS)) {
      consume(IDENTIFIER, "Expect superclass name.");
      superclass = new Expr.Variable(previous());
    }
//< Inheritance not-yet

    List<Stmt.Function> methods = new ArrayList<>();
    consume(LEFT_BRACE, "Expect '{' before class body.");

    while (!check(RIGHT_BRACE) && !isAtEnd()) {
      methods.add(function("method"));
    }

    consume(RIGHT_BRACE, "Expect '}' after class body.");

/* Classes not-yet < Inheritance not-yet
    return new Stmt.Class(name, methods);
*/
//> Inheritance not-yet
    return new Stmt.Class(name, superclass, methods);
//< Inheritance not-yet
  }
//< Classes not-yet

  private Stmt statement() {
//> Control Flow not-yet
    if (match(FOR)) return forStatement();
    if (match(IF)) return ifStatement();
//< Control Flow not-yet
    if (match(PRINT)) return printStatement();
//> Functions not-yet
    if (match(RETURN)) return returnStatement();
//< Functions not-yet
//> Control Flow not-yet
    if (match(WHILE)) return whileStatement();
//< Control Flow not-yet
    if (check(LEFT_BRACE)) return new Stmt.Block(block());

    return expressionStatement();
  }
//> Control Flow not-yet

  private Stmt forStatement() {
    // Parse it.
    consume(LEFT_PAREN, "Expect '(' after 'for'.");

    Stmt initializer;
    if (match(SEMICOLON)) {
      initializer = null;
    } else if (match(VAR)) {
      initializer = varDeclaration();
    } else {
      initializer = expressionStatement();
    }

    Expr condition = null;
    if (!check(SEMICOLON)) {
      condition = expression();
    }
    consume(SEMICOLON, "Expect ';' after loop condition.");

    Stmt increment = null;
    if (!check(RIGHT_PAREN)) {
      increment = new Stmt.Expression(expression());
    }
    consume(RIGHT_PAREN, "Expect ')' after for clauses.");

    Stmt body = statement();

    // Desugar to a while loop.
    if (increment != null) {
      body = new Stmt.Block(Arrays.asList(body, increment));
    }

    if (condition == null) condition = new Expr.Literal(true);
    body = new Stmt.While(condition, body);

    if (initializer != null) {
      body = new Stmt.Block(Arrays.asList(initializer, body));
    }

    return body;
  }

  private Stmt ifStatement() {
    consume(LEFT_PAREN, "Expect '(' after 'if'.");
    Expr condition = expression();
    consume(RIGHT_PAREN, "Expect ')' after if condition.");

    Stmt thenBranch = statement();
    Stmt elseBranch = null;
    if (match(ELSE)) {
      elseBranch = statement();
    }

    return new Stmt.If(condition, thenBranch, elseBranch);
  }
//< Control Flow not-yet

  private Stmt printStatement() {
    Expr value = expression();
    consume(SEMICOLON, "Expect ';' after value.");
    return new Stmt.Print(value);
  }
//> Functions not-yet

  private Stmt returnStatement() {
    Token keyword = previous();
    Expr value = null;
    if (!check(SEMICOLON)) {
      value = expression();
    }

    consume(SEMICOLON, "Expect ';' after return value.");
    return new Stmt.Return(keyword, value);
  }
//< Functions not-yet

  private Stmt varDeclaration() {
    Token name = consume(IDENTIFIER, "Expect variable name.");

    Expr initializer = null;
    if (match(EQUAL)) {
      initializer = expression();
    }

    consume(SEMICOLON, "Expect ';' after variable declaration.");

    return new Stmt.Var(name, initializer);
  }
//> Control Flow not-yet

  private Stmt whileStatement() {
    consume(LEFT_PAREN, "Expect '(' after 'while'.");
    Expr condition = expression();
    consume(RIGHT_PAREN, "Expect '(' after condition.");
    Stmt body = statement();

    return new Stmt.While(condition, body);
  }
//< Control Flow not-yet

  private Stmt expressionStatement() {
    Expr expr = expression();
    consume(SEMICOLON, "Expect ';' after expression.");
    return new Stmt.Expression(expr);
  }
//> Functions not-yet

  private Stmt.Function function(String kind) {
    Token name = consume(IDENTIFIER, "Expect " + kind + " name.");
    consume(LEFT_PAREN, "Expect '(' after " + kind + " name.");
    List<Token> parameters = new ArrayList<>();
    if (!check(RIGHT_PAREN)) {
      do {
        if (parameters.size() >= 8) {
          error(peek(), "Cannot have more than 8 parameters.");
        }

        parameters.add(consume(IDENTIFIER, "Expect parameter name."));
      } while (match(COMMA));
    }
    consume(RIGHT_PAREN, "Expect ')' after parameters.");

    List<Stmt> body = block();
    return new Stmt.Function(name, parameters, body);
  }
//< Functions not-yet

  private List<Stmt> block() {
    consume(LEFT_BRACE, "Expect '{' before block.");
    List<Stmt> statements = new ArrayList<>();

    while (!check(RIGHT_BRACE) && !isAtEnd()) {
      statements.add(declaration());
    }

    consume(RIGHT_BRACE, "Expect '}' after block.");

    return statements;
  }

  private Expr assignment() {
/* Statements and State not-yet < Control Flow not-yet
    Expr expr = equality();
*/
//> Control Flow not-yet
    Expr expr = or();
//< Control Flow not-yet

    if (match(EQUAL)) {
      Token equals = previous();
      Expr value = assignment();

      if (expr instanceof Expr.Variable) {
        Token name = ((Expr.Variable)expr).name;
        return new Expr.Assign(name, value);
//> Classes not-yet
      } else if (expr instanceof Expr.Get) {
        Expr.Get get = (Expr.Get)expr;
        return new Expr.Set(get.object, get.name, value);
//< Classes not-yet
      }

      error(equals, "Invalid assignment target.");
    }

    return expr;
  }
//< Statements and State not-yet
//> Control Flow not-yet

  private Expr or() {
    Expr expr = and();

    while (match(OR)) {
      Token operator = previous();
      Expr right = and();
      expr = new Expr.Logical(expr, operator, right);
    }

    return expr;
  }

  private Expr and() {
    Expr expr = equality();

    while (match(AND)) {
      Token operator = previous();
      Expr right = equality();
      expr = new Expr.Logical(expr, operator, right);
    }

    return expr;
  }
//< Control Flow not-yet
//> equality
  private Expr equality() {
    Expr expr = comparison();

    while (match(BANG_EQUAL, EQUAL_EQUAL)) {
      Token operator = previous();
      Expr right = comparison();
      expr = new Expr.Binary(expr, operator, right);
    }

    return expr;
  }
//< equality
//> comparison
  private Expr comparison() {
    Expr expr = term();

    while (match(GREATER, GREATER_EQUAL, LESS, LESS_EQUAL)) {
      Token operator = previous();
      Expr right = term();
      expr = new Expr.Binary(expr, operator, right);
    }

    return expr;
  }
//< comparison
//> term-and-factor
  private Expr term() {
    Expr expr = factor();

    while (match(MINUS, PLUS)) {
      Token operator = previous();
      Expr right = factor();
      expr = new Expr.Binary(expr, operator, right);
    }

    return expr;
  }

  private Expr factor() {
    Expr expr = unary();

    while (match(SLASH, STAR)) {
      Token operator = previous();
      Expr right = unary();
      expr = new Expr.Binary(expr, operator, right);
    }

    return expr;
  }
//< term-and-factor
//> unary
  private Expr unary() {
    if (match(BANG, MINUS)) {
      Token operator = previous();
      Expr right = unary();
      return new Expr.Unary(operator, right);
    }

/* Parsing Expressions unary < Functions not-yet
    return primary();
*/
//> Functions not-yet
    return call();
//< Functions not-yet
  }
//< unary
//> Functions not-yet
  private Expr finishCall(Expr callee) {
    List<Expr> arguments = new ArrayList<>();
    if (!check(RIGHT_PAREN)) {
      do {
        if (arguments.size() >= 8) {
          error(peek(), "Cannot have more than 8 arguments.");
        }

        arguments.add(expression());
      } while (match(COMMA));
    }

    Token paren = consume(RIGHT_PAREN,
        "Expect ')' after arguments.");

    return new Expr.Call(callee, paren, arguments);
  }

  private Expr call() {
    Expr expr = primary();

    while (true) {
      if (match(LEFT_PAREN)) {
        expr = finishCall(expr);
//> Classes not-yet
      } else if (match(DOT)) {
        Token name = consume(IDENTIFIER,
            "Expect property name after '.'.");
        expr = new Expr.Get(expr, name);
//< Classes not-yet
      } else {
        break;
      }
    }

    return expr;
  }
//< Functions not-yet
//> primary

  private Expr primary() {
    if (match(FALSE)) return new Expr.Literal(false);
    if (match(TRUE)) return new Expr.Literal(true);
    if (match(NIL)) return new Expr.Literal(null);

    if (match(NUMBER, STRING)) {
      return new Expr.Literal(previous().literal);
    }
//> Inheritance not-yet

    if (match(SUPER)) {
      Token keyword = previous();
      consume(DOT, "Expect '.' after 'super'.");
      Token method = consume(IDENTIFIER,
          "Expect superclass method name.");
      return new Expr.Super(keyword, method);
    }
//< Inheritance not-yet
//> Classes not-yet

    if (match(THIS)) return new Expr.This(previous());
//< Classes not-yet
//> Statements and State not-yet

    if (match(IDENTIFIER)) {
      return new Expr.Variable(previous());
    }
//< Statements and State not-yet

    if (match(LEFT_PAREN)) {
      Expr expr = expression();
      consume(RIGHT_PAREN, "Expect ')' after expression.");
      return new Expr.Grouping(expr);
    }
//> primary-error
    // Discard the token so we can make progress.
    advance();

    throw error(previous(), "Expect expression.");
//< primary-error
  }
//< primary
//> match
  private boolean match(TokenType... types) {
    for (TokenType type : types) {
      if (check(type)) {
        advance();
        return true;
      }
    }

    return false;
  }
//< match
//> consume
  private Token consume(TokenType type, String message) {
    if (check(type)) return advance();

    throw error(peek(), message);
  }
//< consume
//> check-and-advance
  private boolean check(TokenType tokenType) {
    if (isAtEnd()) return false;
    return peek().type == tokenType;
  }

  private Token advance() {
    if (!isAtEnd()) current++;
    return previous();
  }
//< check-and-advance
//> utils
  private boolean isAtEnd() {
    return peek().type == EOF;
  }

  private Token peek() {
    return tokens.get(current);
  }

  private Token previous() {
    return tokens.get(current - 1);
  }
//< utils
//> error
  private ParseError error(Token token, String message) {
    Lox.error(token, message);
    return new ParseError();
  }
//< error
//> synchronize
  private void synchronize() {
    while (!isAtEnd()) {
      if (previous().type == SEMICOLON) return;

      switch (peek().type) {
        case CLASS:
        case FUN:
        case VAR:
        case FOR:
        case IF:
        case WHILE:
        case PRINT:
        case RETURN:
          return;
      }

      advance();
    }
  }
//< synchronize
}
