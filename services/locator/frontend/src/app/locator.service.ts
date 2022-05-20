import {Injectable} from '@angular/core';
import {catchError, Observable, of} from "rxjs";
import {HttpClient} from "@angular/common/http";
import {environment} from "../environments/environment";
import {Report} from "./report";

@Injectable({
  providedIn: 'root'
})
export class LocatorService {
  private dropletsUrl = environment.baseUrl;  // URL to web api

  constructor(private http: HttpClient) {
  }

  /** Get id of beacons */
  getList(): Observable<string[]> {
    return this.http.get<string[]>(this.dropletsUrl)
      .pipe(
        catchError(this.handleError<string[]>('getList', []))
      );
  }

  /** Get public info of beacon */
  getPublic(id: string): Observable<string[]> {
    return this.http.get<string[]>(this.dropletsUrl + "/" + id)
      .pipe(
        catchError(this.handleError<string[]>('getPublic', []))
      );
  }

  /** Get private info of beacon */
  getPrivate(id: string, token: string): Observable<Report[]> {
    return this.http.get<Report[]>(this.dropletsUrl + "/" + id + "/private", {headers: {"X-Auth": token}})
      .pipe(
        catchError(this.handleError<Report[]>('getPrivate', []))
      );
  }

  /** Upload new droplet */
  create(name: string): Observable<string> {
    return this.http.put(this.dropletsUrl + "/" + name, null, {responseType: 'text'})
      .pipe(
        catchError(this.handleError<string>('create', "-error-"))
      );
  }

  /**
   * Handle Http operation that failed.
   * Let the app continue.
   *
   * @param operation - name of the operation that failed
   * @param result - optional value to return as the observable result
   */
  private handleError<T>(operation = 'operation', result?: T) {
    return (error: any): Observable<T> => {

      // TODO: send the error to remote logging infrastructure
      console.error(error); // log to console instead

      // Let the app keep running by returning an empty result.
      return of(result as T);
    };
  }
}
