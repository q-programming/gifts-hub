package com.qprogramming.gifts.support;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * Helper class to produce json readable result to all post events
 *
 * @author Khobar
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ResultData {
    private Code code;
    private String message;

    public ResultData(Code code) {
        this.code = code;
    }


    @Override
    public String toString() {
        return this.code + " " + this.message;
    }

    public enum Code {
        OK, WARNING, ERROR
    }

    public static class ResultBuilder {
        private HttpStatus status;
        private ResultData data;

        public ResultBuilder() {
            this.status = HttpStatus.OK;
            this.data = new ResultData();
        }

        public ResultBuilder badReqest() {
            this.status = HttpStatus.BAD_REQUEST;
            return this;
        }

        public ResultBuilder notFound() {
            this.status = HttpStatus.NOT_FOUND;
            return this;
        }


        public ResultBuilder status(HttpStatus status) {
            this.status = status;
            return this;
        }

        public ResultBuilder ok() {
            if (this.data == null) {
                this.data = new ResultData();
            }
            this.data.code = Code.OK;
            return this;
        }

        public ResultBuilder error() {
            if (this.data == null) {
                this.data = new ResultData();
            }
            this.data.code = Code.ERROR;
            return this;
        }

        public ResultBuilder warn() {
            if (this.data == null) {
                this.data = new ResultData();
            }
            this.data.code = Code.WARNING;
            return this;
        }

        public ResultBuilder message(String message) {
            if (this.data == null) {
                this.data = new ResultData();
            }
            this.data.message = message;
            return this;
        }

        public ResponseEntity build() {
            return new ResponseEntity<>(data, status);
        }


    }

}
