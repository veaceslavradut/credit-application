# 7. FORM HANDLING & VALIDATION

### 7.1 Multi-Step Form Pattern

**ApplicationWizard.tsx** - Progressive disclosure form:
```typescript
'use client';

import { useState } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { useApplicationStore } from '@/stores/applicationStore';

// Step-specific schemas
const step1Schema = z.object({
  loanType: z.enum(['personal', 'mortgage', 'auto']),
});

const step2Schema = z.object({
  loanAmount: z.number().min(500).max(100000),
  loanCurrency: z.enum(['MDL', 'EUR', 'USD']),
});

const step3Schema = z.object({
  loanTermMonths: z.number().min(6).max(240),
  rateType: z.enum(['fixed', 'variable']),
});

const step4Schema = z.object({
  annualIncome: z.number().optional(),
  employmentStatus: z.string().optional(),
});

type Step1Data = z.infer<typeof step1Schema>;
type Step2Data = z.infer<typeof step2Schema>;
type Step3Data = z.infer<typeof step3Schema>;
type Step4Data = z.infer<typeof step4Schema>;

export function ApplicationWizard() {
  const [currentStep, setCurrentStep] = useState(1);
  const { draft, updateField } = useApplicationStore();

  // Form hook with step-specific schema
  const { register, handleSubmit, formState: { errors } } = useForm<Step1Data & Step2Data & Step3Data & Step4Data>({
    resolver: zodResolver(
      currentStep === 1 ? step1Schema :
      currentStep === 2 ? step2Schema :
      currentStep === 3 ? step3Schema :
      step4Schema
    ),
    defaultValues: draft,
  });

  const onNext = async (data: any) => {
    // Save current step data
    Object.entries(data).forEach(([key, value]) => {
      updateField(key as any, value);
    });
    setCurrentStep(prev => prev + 1);
  };

  const onSubmit = (data: any) => {
    // Final submission
    console.log('Submit', { ...draft, ...data });
  };

  return (
    <div className="max-w-2xl mx-auto p-6">
      {/* Progress indicator */}
      <div className="mb-8">
        <div className="flex justify-between">
          {[1, 2, 3, 4].map((step) => (
            <div
              key={step}
              className={`flex items-center justify-center w-10 h-10 rounded-full
                ${step <= currentStep
                  ? 'bg-blue-800 text-white'
                  : 'bg-gray-200 text-gray-600'}`}
            >
              {step}
            </div>
          ))}
        </div>
      </div>

      {/* Step 1: Loan Type */}
      {currentStep === 1 && (
        <form onSubmit={handleSubmit(onNext)}>
          <h2 className="text-2xl font-bold mb-6">What type of loan?</h2>
          <div className="space-y-4">
            {['personal', 'mortgage', 'auto'].map((type) => (
              <label key={type} className="flex items-center">
                <input
                  type="radio"
                  value={type}
                  {...register('loanType')}
                  className="mr-3"
                />
                <span className="capitalize">{type}</span>
              </label>
            ))}
          </div>
          {errors.loanType && <p className="text-red-600 mt-2">{errors.loanType.message}</p>}
          <button type="submit" className="mt-6 btn btn-primary">Next</button>
        </form>
      )}

      {/* Step 2: Amount */}
      {currentStep === 2 && (
        <form onSubmit={handleSubmit(onNext)}>
          <h2 className="text-2xl font-bold mb-6">How much do you need?</h2>
          <input
            type="number"
            placeholder="Loan amount"
            {...register('loanAmount', { valueAsNumber: true })}
          />
          {errors.loanAmount && <p className="text-red-600">{errors.loanAmount.message}</p>}
          <div className="flex gap-4 mt-6">
            <button type="button" onClick={() => setCurrentStep(1)} className="btn btn-secondary">Back</button>
            <button type="submit" className="btn btn-primary">Next</button>
          </div>
        </form>
      )}

      {/* Continue for steps 3, 4... */}
    </div>
  );
}
```

### 7.2 Field Validation Strategy

**validators.ts** - Reusable validation functions:
```typescript
import { z } from 'zod';

// Reusable field validators
export const validators = {
  email: z.string().email('Invalid email format'),
  
  password: z
    .string()
    .min(12, 'Password must be at least 12 characters')
    .regex(/[A-Z]/, 'Must contain uppercase letter')
    .regex(/[a-z]/, 'Must contain lowercase letter')
    .regex(/[0-9]/, 'Must contain number')
    .regex(/[!@#$%^&*]/, 'Must contain special character'),
  
  phoneNumber: z
    .string()
    .regex(/^\+?[1-9]\d{1,14}$/, 'Invalid phone number format'),
  
  loanAmount: z
    .number()
    .min(500, 'Minimum loan amount is 500')
    .max(100000, 'Maximum loan amount is 100,000'),
  
  loanTerm: z
    .number()
    .min(6, 'Minimum term is 6 months')
    .max(240, 'Maximum term is 240 months'),
  
  apr: z
    .number()
    .min(0.01, 'APR must be positive')
    .max(100, 'APR cannot exceed 100%'),
};

// Composite validators
export const applicationValidator = z.object({
  loanType: z.enum(['personal', 'mortgage', 'auto']),
  loanAmount: validators.loanAmount,
  loanTermMonths: validators.loanTerm,
  rateType: z.enum(['fixed', 'variable']),
  annualIncome: z.number().optional(),
  consentGiven: z.boolean().refine(v => v === true, {
    message: 'You must consent to data sharing',
  }),
});
```

---
