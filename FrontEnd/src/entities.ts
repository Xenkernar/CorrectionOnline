export interface TestCase {
    input: string,
    output: string
}

export interface LabReport {
    fileName: string
    language: string
    labId: number
    section: string
    templateId: string
    testCases: Map<number, Array<TestCase>>
    publishDateTime: string
    startDateTime: string
    gap: number
    unit: string
    decScore: number
}

export interface Section {
    section: string
    code: string
}

export interface SubmittedReport {
    language: string
    labId: number
    section: string
    studentId: string
    labName: string
    fileName: string
    submitDateTime: string
}
export interface QuestionResult {
    isCorrect: boolean
    detail: string
}

export interface LabResult {
    language: string
    labId: number
    section: string
    studentId: string
    name: string
    reportName: string
    score: number
    questionResults: Map<number,QuestionResult>
}
export interface User {
    id: string
    username: string
    password: string
    section: string
    userRole: string
}
