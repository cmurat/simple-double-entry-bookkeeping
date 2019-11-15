package per.cmurat.other.revolut.core.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import per.cmurat.other.revolut.core.accounting.model.AssetAccount;
import per.cmurat.other.revolut.core.accounting.model.Transaction;
import per.cmurat.other.revolut.core.rest.dto.AssetAccountDto;
import per.cmurat.other.revolut.core.rest.dto.TransactionDto;
import spark.Request;
import spark.Response;
import spark.Spark;

import java.io.PrintWriter;
import java.io.StringWriter;

import static per.cmurat.other.revolut.core.accounting.MathUtils.readableScale;
import static spark.Spark.exception;
import static spark.Spark.get;
import static spark.Spark.path;
import static spark.Spark.post;
import static spark.Spark.put;
import static spark.Spark.stop;

public class RestMapper {

    public static final int STATUS_OK = 200;
    public static final int STATUS_BAD_REQUEST = 400;
    public static final int STATUS_INTERNAL_SERVER_ERROR = 500;

    private static final String EMPTY_BODY = "";

    @Inject
    private AccountingController accountingController;

    public void createMappings() {
        ObjectMapper mapper = new ObjectMapper();

        path("/accounting", () -> {
            put("/account", (request, response) -> {
                final AssetAccountDto requestDto = mapper.readValue(request.body().toString(), AssetAccountDto.class);
                final AssetAccount account = accountingController.createAccount(requestDto.getBalance());
                response.status(STATUS_OK);
                return mapper.writeValueAsString(mapToDto(account));
            });

            get("/account/:id", (request, response) -> {
                final String id = request.params(":id");
                final AssetAccount account = accountingController.getAccount(id);
                response.status(STATUS_OK);
                return mapper.writeValueAsString(mapToDto(account));
            });

            post("/validateTransfer", (request, response) -> {
                final TransactionDto requestDto = mapper.readValue(request.body().toString(), TransactionDto.class);
                accountingController.validateTransfer(requestDto.getSendingAccountId(), requestDto.getReceivingAccountId(), requestDto.getAmount());
                response.status(STATUS_OK);
                return EMPTY_BODY;
            });

            post("/processTransfer", (request, response) -> {
                final TransactionDto requestDto = mapper.readValue(request.body().toString(), TransactionDto.class);
                final Transaction transaction = accountingController.processTransfer(requestDto.getSendingAccountId(), requestDto.getReceivingAccountId(), requestDto.getAmount());
                response.status(STATUS_OK);
                return mapper.writeValueAsString(mapToDto(transaction));
            });
        });

        exception(Exception.class, this::handleException);
        exception(RuntimeException.class, this::handleRuntimeException);
        exception(IllegalArgumentException.class, this::handleIllegalArgumentException);
    }

    public void stopServer() {
        stop();
    }

    private void handleException(final Exception exception, final Request request, final Response response) {
        handleThrowable(exception, request, response);
    }

    private void handleRuntimeException(final RuntimeException exception, final Request request, final Response response) {
        if (exception.getCause() != null && exception.getCause() instanceof IllegalArgumentException) {
            handleIllegalArgumentException((IllegalArgumentException) exception.getCause(), request, response);
        } else {
            handleThrowable(exception, request, response);
        }
    }

    private void handleIllegalArgumentException(final IllegalArgumentException exception, final Request request, final Response response) {
        handleException(exception, request, response);
        response.status(STATUS_BAD_REQUEST);
    }

    private void handleThrowable(final Throwable exception, final Request request, final Response response) {
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

    private TransactionDto mapToDto(final Transaction transaction) {
        final TransactionDto dto = new TransactionDto();
        dto.setSendingAccountId(transaction.getCreditAccount().getId());
        dto.setReceivingAccountId(transaction.getDebitAccount().getId());
        dto.setAmount(transaction.getAmount());
        dto.setDateTime(transaction.getDateTime());

        return dto;
    }

    private AssetAccountDto mapToDto(final AssetAccount account) {
        final AssetAccountDto dto = new AssetAccountDto();
        dto.setId(account.getId());
        dto.setBalance(readableScale(account.getBalance()));

        return dto;
    }
}
