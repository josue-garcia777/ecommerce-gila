type ProblemDetail = {
  title?: string
  detail?: string
  status?: number
}

export class ApiError extends Error {
  readonly status: number
  readonly title: string

  constructor(status: number, title: string, detail: string) {
    super(detail)
    this.status = status
    this.title = title
  }
}

export async function request<T>(url: string, init?: RequestInit): Promise<T> {
  const response = await fetch(url, init)
  if (!response.ok) {
    let problem: ProblemDetail = {}
    try {
      problem = (await response.json()) as ProblemDetail
    } catch {
      problem = {}
    }
    throw new ApiError(
      response.status,
      problem.title ?? 'Request failed',
      problem.detail ?? `The server returned ${response.status}`,
    )
  }
  if (response.status === 204) {
    return undefined as T
  }
  return response.json() as Promise<T>
}

export function jsonRequest(method: string, body?: unknown): RequestInit {
  return {
    method,
    headers: { 'Content-Type': 'application/json' },
    body: body === undefined ? undefined : JSON.stringify(body),
  }
}

export function errorMessage(error: unknown): string {
  if (error instanceof ApiError) {
    return `${error.title}: ${error.message}`
  }
  return error instanceof Error ? error.message : 'An unexpected error occurred'
}
