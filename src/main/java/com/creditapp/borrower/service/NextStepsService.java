package com.creditapp.borrower.service;

import com.creditapp.bank.model.Offer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class NextStepsService {

    public List<String> generateNextSteps(Offer offer, String loanType) {
        List<String> steps = new ArrayList<>();

        switch (loanType.toUpperCase()) {
            case "HOME":
                steps.add("Review loan estimate and closing disclosure");
                steps.add("Schedule home inspection");
                steps.add("Provide homeowner insurance quote");
                steps.add("Complete home appraisal");
                steps.add("Lock interest rate");
                steps.add("Final walkthrough of property");
                steps.add("Sign closing documents");
                steps.add("Wire funds for down payment and closing costs");
                break;

            case "AUTO":
                steps.add("Review loan terms and conditions");
                steps.add("Provide vehicle details and VIN");
                steps.add("Obtain auto insurance quote");
                steps.add("Complete vehicle appraisal or inspection");
                steps.add("Submit proof of income");
                steps.add("Sign loan agreement");
                steps.add("Complete vehicle purchase");
                break;

            case "DEBT_CONSOLIDATION":
                steps.add("Review current debts to consolidate");
                steps.add("Provide list of creditors and balances");
                steps.add("Verify credit report");
                steps.add("Submit income verification");
                steps.add("Agree to settlement with creditors if needed");
                steps.add("Complete consolidation process");
                steps.add("Set up automatic payments");
                break;

            case "STUDENT":
                steps.add("Verify school enrollment status");
                steps.add("Complete FAFSA if applicable");
                steps.add("Review loan disclosure statement");
                steps.add("Complete entrance counseling");
                steps.add("Sign Master Promissory Note");
                steps.add("Funds will be disbursed to your school");
                break;

            case "PERSONAL":
                steps.add("Review loan purpose and terms");
                steps.add("Verify identity with photo ID");
                steps.add("Verify income with recent pay stubs");
                steps.add("Accept loan offer");
                steps.add("Sign promissory note");
                steps.add("Funds will be deposited to your account within 1-2 business days");
                break;

            default:
                steps.add("Review the loan terms and conditions");
                steps.add("Submit proof of income");
                steps.add("Submit valid government-issued ID");
                steps.add("Schedule call with loan officer");
                steps.add("Review and sign all required documents");
        }

        log.debug("Generated next steps for loan type: {}, step count: {}", loanType, steps.size());
        return steps;
    }
}