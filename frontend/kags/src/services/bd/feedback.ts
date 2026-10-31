import type {AxiosResponse} from 'axios'
import http from '@/utils'
import api from '@/api/apiUrls'

export const feedback = {
  fetchFeedback,
  sendFeedback,
  updateFeedback,
  getAuthenticationNumber,
  retrievePassword
}

function fetchFeedback(_surveyId: string): Promise<AxiosResponse<any>> {
  return http.get(api.FEEDBACK_GET.replace('{survey_id}', _surveyId))
}

function sendFeedback(_surveyId: string, _postParameters: unknown): Promise<AxiosResponse<any>> {
  return http.post(api.FEEDBACK_SEND.replace('{survey_id}', _surveyId), _postParameters)
}

function updateFeedback(_surveyId: string, responseId: string, _postParameters: unknown): Promise<AxiosResponse<any>> {
  return http.post(api.FEEDBACK_UPDATE.replace('{survey_id}', _surveyId).replace('{response_id}', responseId), _postParameters)
}

function getAuthenticationNumber(_postParameters: unknown): Promise<AxiosResponse<any>> {
  return http.post(api.REGISTER_SOFTWARE, _postParameters)
}

function retrievePassword(_postParameters: unknown): Promise<AxiosResponse<any>> {
  return http.post(api.RETRIEVE_PASSWORD, _postParameters)
}
