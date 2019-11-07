package per.cmurat.other.revolut.core.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import per.cmurat.other.revolut.core.accounting.model.AssetAccount;
import per.cmurat.other.revolut.core.accounting.model.Transaction;
import per.cmurat.other.revolut.core.rest.dto.AssetAccountDto;
import per.cmurat.other.revolut.core.rest.dto.TransactionDto;
import spark.Request;
import spark.Response;

import java.io.PrintWriter;
import java.io.StringWriter;

import static per.cmurat.other.revolut.core.accounting.MathUtils.readableScale;
import static spark.Spark.exception;
import static spark.Spark.get;
import static spark.Spark.path;
import static spark.Spark.post;
import static spark.Spark.put;

public class RestMapper {

    private static final int STATUS_CREATED = 200;
    private static final int STATUS_BAD_REQUEST = 400;
    private static final int STATUS_INTERNAL_SERVER_ERROR = 500;

    private static final String EMPTY_BODY = "";

    @Inject
    private AccountingController accountingController;

    public void createMappings() {
        ObjectMapper mapper = new ObjectMapper();

        path("/accounting", () -> {
            put("/account", (request, response) -> {
                final AssetAccountDto requestDto = mapper.readValue(request.body().toString(), AssetAccountDto.class);
                final AssetAccount account = accountingController.createAccount(requestDto.getBalance());
                response.status(STATUS_CREATED);
                return mapper.writeValueAsString(mapToDto(account));
            });

            get("/account/:id", (request, response) -> {
                final String id = request.params(":id");
                final AssetAccount account = accountingController.getAccount(id);
                return mapper.writeValueAsString(mapToDto(account));
            });

            post("/validateTransfer", (request, response) -> {
                final TransactionDto requestDto = mapper.readValue(request.body().toString(), TransactionDto.class);
                accountingController.validateTransfer(requestDto.getSendingAccountId(), requestDto.getReceivingAccountId(), requestDto.getAmount());
                return EMPTY_BODY;
            });

            post("/processTransfer", (request, response) -> {
                final TransactionDto requestDto = mapper.readValue(request.body().toString(), TransactionDto.class);
                final Transaction transaction = accountingController.processTransfer(requestDto.getSendingAccountId(), requestDto.getReceivingAccountId(), requestDto.getAmount());
                return mapper.writeValueAsString(mapToDto(transaction));
            });
        });

        exception(Exception.class, this::handleException);
        exception(RuntimeException.class, this::handleRuntimeException);
        exception(IllegalArgumentException.class, this::handleIllegalArgumentException);
    }

    private void handleException(Exception exception, Request request, Response response) {
        handleThrowable(exception, request, response);
    }

    private void handleRuntimeException(RuntimeException exception, Request request, Response response) {
        if (exception.getCause() != null) {
            handleThrowable(exception.getCause(), request, response);
        } else {
            handleThrowable(exception, request, response);
        }
    }

    private void handleIllegalArgumentException(IllegalArgumentException exception, Request request, Response response) {
        handleException(exception, request, response);
        response.status(STATUS_BAD_REQUEST);
    }

    private void handleThrowable(Throwable exception, Request request, Response response) {
        final String stackTrace = getStackTrace(exception);
        System.err.println(stackTrace);
        response.body(stackTrace);
        response.status(STATUS_INTERNAL_SERVER_ERROR);
    }

    private String getStackTrace(final Throwable e) {
        final StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }

    private TransactionDto mapToDto(Transaction transaction) {
        final TransactionDto dto = new TransactionDto();
        dto.setSendingAccountId(transaction.getCreditAccount().getId());
        dto.setReceivingAccountId(transaction.getDebitAccount().getId());
        dto.setAmount(transaction.getAmount());
        dto.setDateTime(transaction.getDateTime());

        return dto;
    }

    private AssetAccountDto mapToDto(AssetAccount account) {
        final AssetAccountDto dto = new AssetAccountDto();
        dto.setId(account.getId());
        dto.setBalance(readableScale(account.getBalance()));

        return dto;
    }
}
