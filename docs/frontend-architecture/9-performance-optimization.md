# 9. PERFORMANCE OPTIMIZATION

### 9.1 Image Optimization

**NextImage wrapper:**
```typescript
import Image from 'next/image';

interface OptimizedImageProps {
  src: string;
  alt: string;
  width?: number;
  height?: number;
  priority?: boolean;
  className?: string;
}

export function OptimizedImage({
  src,
  alt,
  width = 400,
  height = 300,
  priority = false,
  className,
}: OptimizedImageProps) {
  return (
    <Image
      src={src}
      alt={alt}
      width={width}
      height={height}
      priority={priority}
      quality={75} // 75% quality balances size vs visual quality
      placeholder="blur"
      blurDataURL="data:image/svg+xml;base64,..." // Placeholder while loading
      className={className}
    />
  );
}
```

### 9.2 Code Splitting Strategy

**Dynamic imports for heavy features:**
```typescript
import dynamic from 'next/dynamic';

// Lazy load calculator (only needed on that page)
const ScenarioCalculator = dynamic(
  () => import('@/features/calculator/components/ScenarioCalculator'),
  {
    loading: () => <Skeleton lines={5} />,
    ssr: false, // Don't server-render heavy components
  }
);

export function CalculatorPage() {
  return <ScenarioCalculator />;
}
```

### 9.3 Data Fetching Optimization

**React Query cache strategies:**
```typescript
// Keep offer comparison data fresh (5 min) but background refetch
export function useOffers(applicationId: string) {
  return useQuery({
    queryKey: ['offers', applicationId],
    queryFn: () => offerService.list(applicationId),
    staleTime: 5 * 60 * 1000, // 5 minutes
    gcTime: 30 * 60 * 1000,   // Keep in cache 30 min
    refetchOnWindowFocus: false, // Don't refetch on window focus
    refetchOnReconnect: true,    // But do refetch when reconnecting
  });
}

// Keep user session fresh (background)
export function useUser() {
  return useQuery({
    queryKey: ['user'],
    queryFn: () => authService.getProfile(),
    staleTime: 15 * 60 * 1000, // 15 minutes
    refetchInterval: 10 * 60 * 1000, // Refetch every 10 min in background
  });
}
```

### 9.4 Memoization Strategy

**Use React.memo for expensive components:**
```typescript
interface OfferCardProps {
  offer: Offer;
  onSelect: (offerId: string) => void;
  isSelected: boolean;
}

const OfferCard = React.memo(function OfferCard({
  offer,
  onSelect,
  isSelected,
}: OfferCardProps) {
  return (
    <div className={`border-2 rounded-lg p-6 cursor-pointer transition-colors
      ${isSelected ? 'border-blue-600 bg-blue-50' : 'border-gray-200'}`}
      onClick={() => onSelect(offer.id)}
    >
      {/* Offer details */}
    </div>
  );
}, (prevProps, nextProps) => {
  // Custom comparison: only rerender if offer or selection changed
  return prevProps.offer.id === nextProps.offer.id &&
         prevProps.isSelected === nextProps.isSelected;
});
```

### 9.5 Performance Metrics

**Web Vitals tracking:**
```typescript
import { getCLS, getFID, getFCP, getLCP, getTTFB } from 'web-vitals';

function sendMetric(metric: any) {
  // Send to analytics service (Vercel Analytics, Datadog, etc.)
  console.log(metric);
}

getCLS(sendMetric);
getFID(sendMetric);
getFCP(sendMetric);
getLCP(sendMetric);
getTTFB(sendMetric);
```

---
